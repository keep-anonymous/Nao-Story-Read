/**
 * File Name    :MainUI.java
 * Project      :TeacherUI
 * Time         :16:30 31/05/19
 * Author       :LIUYI CHAI
 * Purpose      :This class creates a window for teachers to upload their own books
 */

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JTextPane;


public class UploadUI<ClientUI> extends JFrame {
	private JPanel uploadPanel;
	private JTextField booknameField;
    private MainUI mainframe;
    private String path = "C:\\Users\\Zoe Chai\\Desktop\\nao\\nao_story_read\\NAO CODE\\books";//Chai
//    private String path = "C:\\Users\\Christian Lan\\OneDrive\\NAO CODE\\books";//Lan
    public static String bookTitle = "test";
	
    
    //Initialization of the contents on the window
	public UploadUI() {
		
		setSize(518, 499);
		setLocationRelativeTo(null);
		uploadPanel = new JPanel();
		uploadPanel.setBackground(Color.WHITE);
		uploadPanel.setForeground(new Color(0, 0, 0));
		setContentPane(uploadPanel);
		uploadPanel.setLayout(null);
		
		
		JLabel lblBookTitle = new JLabel("Book Title:");
		lblBookTitle.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblBookTitle.setBounds(16, 150, 91, 35);
		uploadPanel.add(lblBookTitle);
		
		//Initialize a text field for users to edit the book title
		booknameField = new JTextField();
		booknameField.setFont(new Font("Tahoma", Font.PLAIN, 18));
		booknameField.setBounds(105, 150, 347, 35);
		uploadPanel.add(booknameField);
		booknameField.setColumns(10);
		
		
		JButton btnConfirm = new JButton("Confirm");
		btnConfirm.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnConfirm.setBounds(122, 395, 262, 35);
		uploadPanel.add(btnConfirm);
		
		JTextPane tipsPane = new JTextPane();
		tipsPane.setFont(new Font("Dialog", Font.PLAIN, 18));
		tipsPane.setForeground(Color.RED);
		tipsPane.setBounds(16, 201, 441, 132);
		uploadPanel.add(tipsPane);
		
		//Open the file chooser when clicking the "Choose file..." button
		JButton btnuploadfile = new JButton("Choose file...");
		btnuploadfile.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnuploadfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser();
			      
				int rVal = c.showOpenDialog(UploadUI.this);
				if (rVal == JFileChooser.APPROVE_OPTION) {
			    	  
			    	  String fileName = c.getSelectedFile().getName();
			    	  String[] fileExtension = fileName.split("\\.");
			    	  
			    	  //check if the chosen file is a pdf file
			    	  if (fileExtension[fileExtension.length - 1].equals("pdf")){
			    		  tipsPane.setText(null);
			    	      booknameField.setText(fileName);
			    	      //set the confirm button
			    	      btnConfirm.addActionListener(new ActionListener() {
			    				public void actionPerformed(ActionEvent e) {
			    					File bookfile = c.getSelectedFile();
			    					File copyfile = new File(path + "\\" +booknameField.getText());
			    					    //check if the file already exists
			    						if(copyfile.exists()){
			    							JOptionPane.showMessageDialog(null, "Oops! This book already exists.");
			    						}
			    						
			    						else{
			    							try {
												copyfile.createNewFile();
											
			    							    copyFile(bookfile, copyfile);
			    							    JOptionPane.showConfirmDialog(null, "Book successfully uploaded! You can choose it from the book menu.");
			    		                        
					    					    //add the new uploaded book to the JList
					    					    bookTitle= getBookTitle(fileName);
					    					    mainframe.listModel.addElement(booknameField.getText());
					    					    
					    					    
			    							    
			    							} catch (IOException e1) {
												    // TODO Auto-generated catch block
												    e1.printStackTrace();
											}
			    						}
									
			    				}
			    			});
			    	  }
			    	  else{
			    		  tipsPane.setText("*The file you uploaded shoud be a pdf file.");
			    	  }
			        
			      }
				
			     if (rVal == JFileChooser.CANCEL_OPTION) {
			    	 tipsPane.setText("*Please choose a pdf file to upload.");
			    	 
			      }
			}
		});
		btnuploadfile.setBounds(6, 29, 475, 41);
		uploadPanel.add(btnuploadfile);
		
		}
	
//This function copies a file to restore it
private void copyFile(File source, File dest) throws IOException {    
    FileChannel inputChannel = null;    
    FileChannel outputChannel = null;    
    try {
        inputChannel = new FileInputStream(source).getChannel();
        outputChannel = new FileOutputStream(dest).getChannel();
        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
    } finally {
        inputChannel.close();
        outputChannel.close();
    }
}

//This function removes the ".dpf" from the file name
public String getBookTitle(String fileName){
	bookTitle= fileName.substring(0, fileName.length()-4);
	return bookTitle;
		
	}



}

//:~