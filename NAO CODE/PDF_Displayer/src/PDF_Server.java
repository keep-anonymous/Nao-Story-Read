/**
 * File Name    :PDF_Server.java
 * Project      :PDF_Displayer
 * Time         :16:30 31/05/19
 * Author       :LIUYI CHAI for the UI part
 *               XINJIE LAN implemented auto-starting(Threads & startNao()) and Connection(start()) 
 * Purpose      :This class creates a socket to receive the messages sent from the client, builds
 *               multiple threads to auto-launch the python file on the client's end, and creates 
 *               the main window of the pdf displayer
 */
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.JList;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ImageIOUtil;

public class PDF_Server {
	private JFrame mainframe;
	private JLabel imgLabel;
	private String path = "C:\\Users\\Zoe Chai\\Desktop";//Chai
//	private String path = "C:\\Users\\Christian Lan\\OneDrive\\NAO CODE";//Lan
	public static List<String> bookList = new ArrayList<>();
	public static JList<String> list = new JList<String>(new DefaultListModel<String>());
	public static File bookText;
	
	private ServerSocket listeningSocket;
	private static final int PORT = 5555;
	private int numOfClients;
	
	public static void main(String[] args) throws IOException {
		
		PDF_Server server= new PDF_Server();
		Thread thread1 = new Thread() {
            public void run() {
            	server.mainframe.setVisible(true);
        		server.start();
            }
        };
        Thread thread2 = new Thread() {
            public void run() {
            	server.startNao();
            }
        };
        thread1.start();       
        thread2.start();

        

    }
		
	
    
	public PDF_Server() throws IOException {
		    System.out.println("initializing the server...");
		    listeningSocket = new ServerSocket(8088);
			
		    initialize();
			
		
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize() {
		mainframe = new JFrame();
		mainframe.getContentPane().setBackground(Color.WHITE);
		mainframe.getContentPane().setFont(new Font("Cooper Black", Font.BOLD, 19));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int)screenSize.getWidth();
		int height = (int)screenSize.getHeight();
		
		mainframe.setSize(width,height);
		mainframe.setLocationRelativeTo(null);
		mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainframe.getContentPane().setLayout(null);
		
		imgLabel = new JLabel("Waiting for the robot to connect......",SwingConstants.CENTER);
		imgLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 30));
		imgLabel.setBounds(mainframe.getBounds());
		mainframe.getContentPane().add(imgLabel);
		
	}
	
	
	
	public void requests(Socket socket){
			try {
	
				
				BufferedReader reader;
				String line;
				String clientMsg = null;
				
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                
				//get the pages and book title
				String[] pages = getBookPages();
				List<String> pageArray = new ArrayList<>(); 
				for(int i=1;i<pages.length;i++){
					pageArray.add(pages[i]);
					System.out.println("pageArray:"+pageArray.get(i-1));
				}
				
				String bookTitle = getBookTitle();
				
				imgLabel.setText("Connection established. Preparing the book file......");
				
				convert(bookTitle);//parse the pdf file
				
				
				
				//set the book cover as the first page
				imgLabel.setText("");
				Image pageImg = new ImageIcon(path + "\\imgs\\0.png").getImage();
				Image scaledImage = pageImg.getScaledInstance(imgLabel.getWidth(),imgLabel.getHeight(),Image.SCALE_SMOOTH);
				ImageIcon icon = new ImageIcon(scaledImage);
				imgLabel.setIcon(icon);
				
				System.out.println("3");//test3
				
				
		        //turn page when receiving the "turn" message
		        while((clientMsg = reader.readLine()) != null) {
		        	   System.out.println("get0:"+pageArray.get(0));
					   int pagenum = Integer.parseInt(pageArray.get(0));
					   
					   //check if the first chosen page is the book cover
					   if(pagenum==0){
						   pageArray.remove(pageArray.get(0));
						   pagenum = Integer.parseInt(pageArray.get(0));
					   }
					   turnPage(pagenum);
					   pageArray.remove(pageArray.get(0));
					   System.out.println(clientMsg);
				} 
			
				// TODO Auto-generated catch block
				
			
				
			}catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IndexOutOfBoundsException ignore) { 
					
				}
			
			//Do turn page logic here when get msg from robot
		}
	
	public void start(){
		try {
			//Create a server socket listening on port 8080
			listeningSocket = new ServerSocket(PORT);
			Socket clientSocket = null;
			
			numOfClients = 0; //counter to keep track of the number of clients
			
			//Listen for incoming connections for ever 
			while (true) {
				System.out.println("port: "+ PORT);
				System.out.println("Server listening on port "+PORT+" for a connection"+"\n");
				//Accept an incoming client connection request 
				//This method will block until a connection request is received
				clientSocket = listeningSocket.accept(); 
				System.out.println("Connection Established");
				
				numOfClients++;
				
				requests(clientSocket);
				
				
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			if(listeningSocket != null) {
				try {
					listeningSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
//auto-start the python file
private void startNao() {
	String pathNao="\"C:\\Users\\Zoe Chai\\Desktop\\nao\\nao_story_read\\NAO CODE\\";
	String py="naoMain";
	Runtime rt = Runtime.getRuntime();
	try {
		Process python = rt.exec("python "+pathNao + py + ".py\"");
		BufferedReader br = new BufferedReader(new InputStreamReader(python.getInputStream())); 

	    String line;
	    while ((line = br.readLine()) != null) {
	        System.out.println(line);
	    }
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
	
//read the book_pages.txt file to get the pages
private String[] getBookPages() throws IOException{
	String[] pages = null;
	File book_pages = new File(path + "\\books\\book_pages.txt");
	if(!book_pages.exists()){
		System.out.println("There is no chosen book.");
	}else{
        String lines = null;
        FileReader fileReader = new FileReader(path + "\\books\\book_pages.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        int count = 0;
        while((lines=bufferedReader.readLine()) != null) {
        	if(count == 1){
        		String bookPagesStr = "["+lines.trim();
            	String regex = "\\D+";
            	pages = bookPagesStr.split(regex);//get a string array of the page numbers
            	
            	System.out.println(pages[0]);
            	
        	}
        	count++;
        }   

        bufferedReader.close();         
    }
	return pages;
}

//read the book_pages.txt file to get the books
private String getBookTitle() throws IOException{
		String bookTitle = null;
		File book_pages = new File(path + "\\books\\book_pages.txt");
		if(!book_pages.exists()){
			System.out.println("There is no chosen book.");
		}else{
          String line = null;
          FileReader fileReader = new FileReader(path + "\\books\\book_pages.txt");
          BufferedReader bufferedReader = new BufferedReader(fileReader);
          int count = 0;
          while((line=bufferedReader.readLine()) != null) {
          	if(count == 0){
          		bookTitle = line.trim();
          	}
          	count++;
          	
          	
          }   

          bufferedReader.close();         
      }return bookTitle;
}

//convert the pdf pages to images using pdfbox
private void convert(String bookTitle){
		try {
			
			File pdfFile = new File(path + "\\books\\"+ bookTitle);
			if(!pdfFile.exists()){
				System.out.println("Book not found.");
			}else{
				PDDocument document = PDDocument.loadNonSeq(pdfFile, null);
				List<PDPage> pdPages = document.getDocumentCatalog().getAllPages();
				int page = 0;
				
				File folder = new File(path + "\\imgs");
				folder.mkdir();
				if(folder.exists()==false){
					folder.createNewFile();
				}
				
				for (PDPage pdPage : pdPages)
				{ 
				    
				    BufferedImage bim = pdPage.convertToImage(BufferedImage.TYPE_INT_RGB, 300);
				    ImageIOUtil.writeImage(bim,"png", path + "\\imgs\\" + page,BufferedImage.TYPE_INT_RGB, 300);
				    ++page;
				}
				document.close();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			///e1.printStackTrace();
		}
		}
//set the pane with images
private void turnPage(int pagenum){
	Image pageImg = new ImageIcon(path + "\\imgs\\" + pagenum + ".png").getImage();
	Image scaledImage = pageImg.getScaledInstance(imgLabel.getWidth(),imgLabel.getHeight(),Image.SCALE_SMOOTH);
	ImageIcon icon = new ImageIcon(scaledImage);
	imgLabel.setIcon(icon);
}
	
}




