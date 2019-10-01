/**
 * File Name    :MainUI.java
 * Project      :TeacherUI
 * Time         :16:30 31/05/19
 * Author       :LIUYI CHAI
 * Purpose      :This class creates the main window of the teacher's interface
 */



import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JList;

public class MainUI {
	private UploadUI uploadframe;
	private JFrame mainframe;
	private ChoosePageUI choosepageframe;
	private JTextField searchField;
	public static ArrayList bookList = new ArrayList();
	public static DefaultListModel listModel = new DefaultListModel<>();
	public static JList<String> list = new JList();
	public static File bookText;
	private static File folder;
	private static String path = "C:\\Users\\Zoe Chai\\Desktop\\nao\\nao_story_read\\NAO CODE\\books";//Chai
//  private static String path = "C:\\Users\\Christian Lan\\OneDrive\\NAO CODE\\books";//Lan
	
	
	
    //A constructor to run the initialize function
	public MainUI() {
	
			initialize();
			
		
	}
	
	//This function initializes the main window with basic contents on it 
	public void initialize() {
		mainframe = new JFrame();
		mainframe.getContentPane().setBackground(Color.WHITE);
		mainframe.getContentPane().setFont(new Font("Cooper Black", Font.BOLD, 19));
		mainframe.setSize(766, 573);
		mainframe.setLocationRelativeTo(null);
		mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainframe.getContentPane().setLayout(null);
		
		//The search field is for users to filter the the book titles
		searchField = new JTextField();
		searchField.setBounds(37, 16, 545, 37);
		mainframe.getContentPane().add(searchField);
		searchField.setColumns(10);
		searchField.setFont(new Font("Tahoma", Font.PLAIN, 18));
		

		
		//close the main window and open the upload window when clicking "upload my books"
		JButton btnChooseYourBooks = new JButton("Upload my books");
		btnChooseYourBooks.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnChooseYourBooks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uploadframe = new UploadUI();
				uploadframe.setVisible(true);
			}
		});
		btnChooseYourBooks.setBounds(526, 60, 188, 37);
		mainframe.getContentPane().add(btnChooseYourBooks);
		
		//Initialization of "Choose a book..." label
		JLabel lblNewLabel_1 = new JLabel("Choose a book...");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblNewLabel_1.setBounds(37, 63, 138, 37);
		mainframe.getContentPane().add(lblNewLabel_1);
		
		
		
	}
	
	public static void main(String[] args) throws IOException {
	    
		//Create a new object of MainUI and set the main frame visible
		MainUI mainWindow = new MainUI();
		mainWindow.mainframe.setVisible(true);
		
		//Initialization of the JList to show all the book titles
		mainWindow.mainframe.getContentPane().add(list);
		list.setBounds(37, 101, 677, 359);
		list.setFont(new Font("Tahoma", Font.PLAIN, 18));
		list.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		mainWindow.bindData();
		
		//Choose a book and click on the "Confirm" button to open a new frame to choose pages
		JButton btnComfirm = new JButton("Comfirm");
		btnComfirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    if(list.getSelectedValue() == null){
			    	JOptionPane.showConfirmDialog(null, "You didn't choose any book.");
			    }
			    else{
			    	ChoosePageUI choosepageframe = new ChoosePageUI();
					choosepageframe.setVisible(true);
			    }
				
			}
		});
		btnComfirm.setBounds(561, 464, 153, 37);
		btnComfirm.setFont(new Font("Tahoma", Font.PLAIN, 18));
		mainWindow.mainframe.getContentPane().add(btnComfirm);
		
		//Initialization of the "Search" button
		JButton btnSearch = new JButton("Search");
		btnSearch.setBounds(586, 16, 128, 33);
		btnSearch.setFont(new Font("Tahoma", Font.PLAIN, 18));
		mainWindow.mainframe.getContentPane().add(btnSearch);
		btnSearch.addActionListener(new ActionListener() {
			//Filter the book titles with the characters typed in the search field
			public void actionPerformed(ActionEvent e) {
				String searchText = mainWindow.searchField.getText().trim();
				try {
					mainWindow.searchFilter(searchText);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		
		
				
	}
	
//This function gets all the book names in the file folder and returns them as an array list
private ArrayList getTitles() throws IOException {
	
	folder = new File(path);
	folder.mkdir();
	if(folder.exists()==false){
		folder.createNewFile();
	}
	File[] fileLists = folder.listFiles();
	ArrayList bookList = new ArrayList();
	if(fileLists.length != 0){
		for (int i = 0; i < fileLists.length; i++) {
			//get all the pdf files
		    if(fileLists[i].isFile()&&fileLists[i].getName().contains("pdf")) {
		    	//remove the ".pdf" and add the book title to the array list
		    	bookList.add(fileLists[i].getName().substring(0, fileLists[i].getName().length()-4));
		    }
		}
    }
	return bookList;
}

//This function adds the book titles to the JList
private void bindData() throws IOException {
	getTitles().stream().forEach((title)->{
	listModel.addElement(title);
	});
	list.setModel(listModel);
	list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
}

//This function filters the book titles and adds the books searched in the search field to the JList
private void searchFilter(String searchText) throws IOException {
	DefaultListModel filterModel = new DefaultListModel();
	ArrayList bookList = getTitles();
	bookList.stream().forEach((title)->{
		String titleName = title.toString().toLowerCase();
		if(titleName.contains(searchText.toLowerCase())){
			filterModel.addElement(title);
		}
	});
	listModel=filterModel;
	list.setModel(listModel);
}
}


