/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;
import controller.ClientFrame;
import static controller.ClientFrame.nickname;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.UserDatabase;
import javax.swing.text.StyleConstants;


import javax.swing.JTextField;
import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import controller.ClientFrame;
import static controller.ClientFrame.nickname;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import server.UserDatabase;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java 
 */

public class ContactHorsLigne1 extends javax.swing.JFrame {

    /**
     * Creates new form ContactHorsLigne
     */
    private String sender ; 
     private String reciever ; 
      StyledDocument sDoc;
       Style defaut;

    public ContactHorsLigne1(String s) {
        this.reciever=s;
        System.out.println("recepteur"+s);
        initComponents();
    }
    public String getUsername(){
        return this.UsernameHorsLigne.toString() ; 
    }
    public String getMessage(){
        return this.MessageHorsLigne.toString();
    }
    public void setSender(String sender){
        this.sender = sender ; 
    }

  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setText("Envoyer");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        UsernameHorsLigne = new javax.swing.JTextPane();
        MessageHorsLigne = new javax.swing.JTextPane();
        
        UsernameHorsLigne.setText(reciever);
        UsernameHorsLigne.setEditable(false);
        MessageHorsLigne.setEditable(true);
        
         textPane = new JTextPane();
        // textPane.setEditable(false);
         //this.sDoc = (StyledDocument)textPane.getDocument();
           //defaut  = textPane.getStyle("default");
           
           //
         
 sDoc = textPane.getStyledDocument();

 





        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(layout.createSequentialGroup()
        			.addGap(240)
        			.addComponent(jButton1)
        			.addContainerGap(240, Short.MAX_VALUE))
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap(31, Short.MAX_VALUE)
        			.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
        				.addComponent(textPane, Alignment.LEADING)
        				.addComponent(MessageHorsLigne, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
        				.addComponent(UsernameHorsLigne, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE))
        			.addGap(32))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGap(26)
        			.addComponent(UsernameHorsLigne, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
        			.addGap(28)
        			.addComponent(textPane, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(MessageHorsLigne, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
        			.addComponent(jButton1)
        			.addGap(40))
        );
        getContentPane().setLayout(layout);

        pack();
    }// </editor-fold>                        

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {           
        InsertInfo();
        this.MessageHorsLigne.setText("");
    }                                        

    public void InsertInfo(){
         PreparedStatement stt;
         Connection con;
         try{
             Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat_db","root","");
            String username = reciever ;
            String message = MessageHorsLigne.getText() ;
             String sql = "insert into message (message,sender,reciever) values('"+message+"','"+this.sender+"','"+username+"')  ";
            
            stt = con.prepareStatement(sql);
            stt.execute();
            stt.close();
            //JTextPane.add(JTextPane.setText(message));
             //int pos = 0;
          
            //this.sDoc.insertString(pos, message, defaut);
            //StyleConstants.setLineSpacing(defaut, 0.2f);
            SimpleAttributeSet keyWord = new SimpleAttributeSet();

            StyleConstants.setBold(keyWord, true);
            try
{
    sDoc.insertString(sDoc.getLength(), message+"\n", keyWord );
}
catch(Exception e) { System.out.println(e); }
  
          
           
          


            
           
            
         }
         catch(Exception e ){
             System.out.println(e.getMessage());
         }
                    
                                       
                                             
    
    
    }
    
    
    
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ContactHorsLigne1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ContactHorsLigne1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ContactHorsLigne1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ContactHorsLigne1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                String s = null;
                new ContactHorsLigne1(s).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JTextPane MessageHorsLigne;
    private javax.swing.JTextPane UsernameHorsLigne;
    private javax.swing.JButton jButton1;
    JTextPane textPane;

}
