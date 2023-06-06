/*

 */
package controller;

/**
 *

 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import server.UserDatabase;
import view.ClientPanel;
import view.ContactHorsLigne1;
import view.InfoContactFrame;
import view.InfoContactFrame1;
import view.ListeContactPanel;
import view.LoginPanel;
import view.PrivateChat;
import view.RoomPanel;
import view.SignUpPanel;
import view.WelcomePanel;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JOptionPane;
import java.util.logging.Level;
import java.util.logging.Logger;


/**

 */

public class ClientFrame extends JFrame implements Runnable {
    String serverHost;
    List<String> AllUsersList = new ArrayList<>();
    List<String> OnlineUsersList = new ArrayList<>();
    public static final String NICKNAME_EXIST = "Ce nom d'utilisateur est déjà utilisé, veuillez entrer un nom d'utilisateur correct !";
    public static final String NICKNAME_VALID = "Nom d'utilisateur accepté";
    public static final String NICKNAME_INVALID = "Nom d'utilisateur ou mot de passe incorrecte";
    public static final String SIGNUP_SUCCESS = "Connexion réussie";
    public static final String ACCOUNT_EXIST = "Ce compte existe déjà !";
    public static String nickname;
    String name;
    String room;
    Socket socketOfClient;
    BufferedWriter bw;
    BufferedReader br;
    PrivateKey  privateKey;
    public String aeskey;
    
    JPanel mainPanel;
    LoginPanel loginPanel;
    ClientPanel clientPanel;
    WelcomePanel welcomePanel;
    SignUpPanel signUpPanel;
    RoomPanel roomPanel;
    InfoContactFrame1 infocontactframe1;
    ListeContactPanel listecontactpanel;
    
    Thread clientThread;
    boolean isRunning;
    
    JMenuBar menuBar;
    JMenu menuAccount;
    JMenuItem  itemLogout;
    
    SendFileFrame sendFileFrame;
    
    StringTokenizer tokenizer;
    String myDownloadFolder;
    
    Socket socketOfSender, socketOfReceiver;
    
    rsa RSA = new rsa();
    aes AES = new aes();
    
    DefaultListModel<String> listModel, listModelThisRoom, listModel_rp , listHorsLigne;
        
    boolean isConnectToServer;
    
   static int timeClicked = 0; 
   static int time =0;   
    
    static  Hashtable<String, PrivateChat> listReceiver;
    
    public ClientFrame(String name) {
        this.name = name;
        socketOfClient = null;
        bw = null;
        br = null;
        isRunning = true;
        listModel = new DefaultListModel<>();
        listHorsLigne = new DefaultListModel<>();
        
        listModelThisRoom = new DefaultListModel<>();
        listModel_rp = new DefaultListModel<>();
        isConnectToServer = false;
        listReceiver = new Hashtable<>();
        
        mainPanel = new JPanel();
        loginPanel = new LoginPanel();
        clientPanel = new ClientPanel();
        welcomePanel = new WelcomePanel();
        signUpPanel = new SignUpPanel();
        roomPanel = new RoomPanel();
        listecontactpanel = new ListeContactPanel();
       
        
       
        welcomePanel.setVisible(true);
        signUpPanel.setVisible(false);
        loginPanel.setVisible(false);
        roomPanel.setVisible(false);
        clientPanel.setVisible(false);
        
        mainPanel.add(welcomePanel);
        mainPanel.add(signUpPanel);
        mainPanel.add(loginPanel);
        mainPanel.add(roomPanel);
        mainPanel.add(clientPanel);
        
        addEventsForWelcomePanel();
        addEventsForSignUpPanel();
        addEventsForLoginPanel();
        addEventsForClientPanel();
        addEventsForRoomPanel();
        addEventsForListeContactPanel();
     
        
        menuBar = new JMenuBar();  
        menuAccount = new JMenu();
        itemLogout = new JMenuItem();
        
        menuAccount.setText("Déconnexion");
        itemLogout.setText("Déconnexion");
        
        
       
        
        menuAccount.add(itemLogout);        
        menuBar.add(menuAccount);
        
        itemLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int kq = JOptionPane.showConfirmDialog(ClientFrame.this, "Êtes-vous sûr de vouloir vous déconnecter?", "Alerte", JOptionPane.YES_NO_OPTION);
                if(kq == JOptionPane.YES_OPTION) {
                    try {
                        isConnectToServer = false;
                        socketOfClient.close();
                        ClientFrame.this.setVisible(false);
                    } catch (IOException ex) {
                        Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    new ClientFrame(null).setVisible(true);
                }
            }
        });

        menuBar.setVisible(false);
        
        setJMenuBar(menuBar);
        pack();
        
        add(mainPanel);
        setSize(570, 520);
        setLocation(400, 100);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle(name);
    }
    
    
    private void addEventsForWelcomePanel() {
        
        welcomePanel.getBtLogin_welcome().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                welcomePanel.setVisible(false);
                signUpPanel.setVisible(false);
                loginPanel.setVisible(true);
                clientPanel.setVisible(false);
                roomPanel.setVisible(false);
            }
        });
        welcomePanel.getBtSignUp_welcome().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                welcomePanel.setVisible(false);
                signUpPanel.setVisible(true);
                loginPanel.setVisible(false);
                clientPanel.setVisible(false);
                roomPanel.setVisible(false);
            }
        });
        
    }

        private void addEventsForSignUpPanel() {
        signUpPanel.getLbBack_signup().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                welcomePanel.setVisible(true);
                signUpPanel.setVisible(false);
                loginPanel.setVisible(false);
                clientPanel.setVisible(false);
                roomPanel.setVisible(false);
            }
        });
        signUpPanel.getBtSignUp().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                btSignUpEvent();
            }
        });
    }
    
    
    
    
    private void addEventsForListeContactPanel() {
        listecontactpanel.getjLabel4().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                welcomePanel.setVisible(false);
                signUpPanel.setVisible(false);
                loginPanel.setVisible(false);
                clientPanel.setVisible(false);
                roomPanel.setVisible(true);
            }
        });
    }

    private void addEventsForLoginPanel() {
        loginPanel.getTfNickname().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                if(ke.getKeyCode() == KeyEvent.VK_ENTER) btOkEvent();
            }
            
        });
        loginPanel.getTfPass().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                if(ke.getKeyCode() == KeyEvent.VK_ENTER) btOkEvent();
            }
            
        });
        loginPanel.getBtOK().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                btOkEvent();
            }
        });
        loginPanel.getLbBack_login().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                welcomePanel.setVisible(true);
                signUpPanel.setVisible(false);
                loginPanel.setVisible(false);
                clientPanel.setVisible(false);
                roomPanel.setVisible(false);
            }
        });
    }
   
   
    

    private void addEventsForClientPanel() {
        clientPanel.getBtSend().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                btSendEvent();
            }
        });
     
        clientPanel.getTaInput().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                if(ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    btSendEvent();
                    btClearEvent();
                }
            }
        });
        clientPanel.getLbLike().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|LIKE");
            }
        });
        clientPanel.getLbDislike().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|DISLIKE");
            }
        });
        clientPanel.getLbPacMan().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|PAC_MAN");
            }
        });
        clientPanel.getLbCry().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|CRY");
            }
        });
        clientPanel.getLbGrin().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|GRIN");
            }
        });
        clientPanel.getLbSmile().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|SMILE");
            }
        });
        clientPanel.getLbAngel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|ANGEL");
            }
        });
        clientPanel.getLbAngry().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|ANGRY");
            }
        });
        clientPanel.getLbSmilee().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|SMILEE");
            }
        });
        clientPanel.getLbConsider().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|CONSIDER");
            }
        });
        /*clientPanel.getLbCute().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|CUTE");
            }
        });
        clientPanel.getLbHaha().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|HAHA");
            }
        });
        clientPanel.getLbHeart().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|HEART");
            }
        });
        clientPanel.getLbLol().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|LOL");
            }
        });
        clientPanel.getLbLove().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|LOVE");
            }
        });
        clientPanel.getLbVomissement().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                sendToServer("CMD_ICON|VOMISSEMENT");
            }
        }); */
        clientPanel.getOnlineList().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                openPrivateChatInsideRoom();
            }
        });
        
        /* */
        clientPanel.getjLabel2().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                welcomePanel.setVisible(false);
                signUpPanel.setVisible(false);
                loginPanel.setVisible(false);
                clientPanel.setVisible(false);
                roomPanel.setVisible(true);
            }
        });
   
    }  
    
    
    private void addEventsForRoomPanel() {
        roomPanel.getLbRoom1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                ClientFrame.this.room = roomPanel.getLbRoom1().getText();
                labelRoomEvent();
            }
        });
        roomPanel.getLbRoom2().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                ClientFrame.this.room = roomPanel.getLbRoom2().getText();
                labelRoomEvent();
            }
        });
        /*roomPanel.getLbRoom3().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                ClientFrame.this.room = roomPanel.getLbRoom3().getText();
                labelRoomEvent();
            }
        }); */
        /*roomPanel.getLbRoom4().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                ClientFrame.this.room = roomPanel.getLbRoom4().getText();
                labelRoomEvent();
            }
        });    */
        roomPanel.getOnlineList_rp().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                ListMenu();
                openPrivateChatOutsideRoom();
            }
        });
        
                roomPanel.getListHorsLigne().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                openMessgeHorsLigne();
            }
        });
    }
    public void openPrivateChatOutsideRoom(String clickedUserName) {
    	if (clickedUserName.equals(ClientFrame.this.name)) {
            JOptionPane.showMessageDialog(ClientFrame.this, "Vous ne pouvez pas envoyer un message a vous meme.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StyledDocument document = new DefaultStyledDocument();
       
            PrivateChat pc = listReceiver.get(clickedUserName);
            if(pc == null) {   
                pc = new PrivateChat(name, clickedUserName, serverHost, bw, br,aeskey);
                
                pc.getLbReceiver().setText(" \""+pc.receiver+"\"");
                pc.setTitle(pc.receiver);
                pc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                pc.setVisible(true);

                listReceiver.put(clickedUserName, pc);
               
            } else {
                pc.setVisible(true);
            }
    }
   
    Runnable countinga = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            timeClicked = 0;
        }
    };
    
    /*
    Ce code ajoute des événements aux éléments de la fenêtre "roomPanel" dans l'interface graphique de l'application de chat. Il y a plusieurs événements associés à différentes actions de l'utilisateur :
    Lorsqu'un utilisateur clique sur l'un des éléments de la liste des salles (lbRoom1, lbRoom2, lbRoom4), cela met à jour la variable "room" dans la classe ClientFrame et appelle la méthode "labelRoomEvent()" pour changer la vue de la fenêtre de la salle à la fenêtre du chat client.
    Lorsqu'un utilisateur clique sur l'un des noms d'utilisateurs en ligne (onlineList_rp), cela appelle la méthode "openPrivateChatOutsideRoom()" pour ouvrir une fenêtre de chat privé avec l'utilisateur sélectionné. Cette fonction vérifie également si l'utilisateur a essayé de se contacter lui-même.
    Le Runnable "countinga" est un compteur pour suivre le temps entre les clics de l'utilisateur. Il est utilisé pour contrôler les événements de double-clic ou simple-clic.
    En somme, ce code permet à l'utilisateur de naviguer entre les salles de chat, d'ouvrir une fenêtre de chat privé avec un utilisateur en ligne et contrôle les événements de clic de l'utilisateur.
    */



    public  void open(String receiver,String aeskey) {
    	 if(time == 0) {
             Thread countingTo500ms = new Thread(counting);
             countingTo500ms.start();
         }

         if(time == 0) {  
             String privateReceiver = receiver;
             PrivateChat pc = listReceiver.get(privateReceiver);
             if(pc == null) {    
                 pc = new PrivateChat(name, privateReceiver, serverHost, bw, br,aeskey);
                 
                 pc.setTitle(pc.receiver);
                 pc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                 pc.setVisible(true);

                 listReceiver.put(privateReceiver, pc);
             } else {
                 pc.setVisible(true);
             }
         }
         
     }
    
        static Runnable counting = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            timeClicked = 0;
        }
    };
    
    
    
    private void openPrivateChatInsideRoom() {
        timeClicked++;
        if(timeClicked == 1) {
            Thread countingTo500ms = new Thread(countinga);
            countingTo500ms.start();
        }

        if(timeClicked == 2) {  
            String nameClicked = clientPanel.getOnlineList().getSelectedValue();
            if(nameClicked.equals(ClientFrame.this.name)) {
                JOptionPane.showMessageDialog(ClientFrame.this, "Vous ne pouvez pas envoyer un message à vous même !", "Attention", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if(!listReceiver.containsKey(nameClicked)) {    
                PrivateChat pc = new PrivateChat(name, nameClicked, serverHost, bw, br,aeskey);

                pc.getLbReceiver().setText(pc.receiver);
                pc.setTitle(pc.receiver);
                pc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                pc.setVisible(true);

                listReceiver.put(nameClicked, pc);
            } else {
                PrivateChat pc = listReceiver.get(nameClicked);
                pc.setVisible(true);
            }
        }
    }
    // cette methode est utilisée pour ouvrir la fenètre de chat privé entre deux utilisateurs
    private void openPrivateChatOutsideRoom() {
        timeClicked++;
        if(timeClicked == 1) {
            Thread countingTo500ms = new Thread(counting);
            countingTo500ms.start();
        }

        if(timeClicked == 2) { 
           
            String privateReceiver = roomPanel.getOnlineList_rp().getSelectedValue();
            PrivateChat pc = listReceiver.get(privateReceiver);
            if(pc == null) {   
                pc = new PrivateChat(name, privateReceiver, serverHost, bw, br,aeskey);
                pc.getLbReceiver().setText("\""+pc.receiver+"\"");
                pc.setTitle(pc.receiver);
                pc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                pc.setVisible(true);

                listReceiver.put(privateReceiver, pc);
            } else {
                pc.setVisible(true);
            }
        }
    }
    
    //============================================================================================================
    //cette methode est utilisée pour afficher une fenètre permettant à l'utilisateur d'envoyer des 
    // messages à ces contact hors ligne 
     private void openMessgeHorsLigne() {
        timeClicked++;
        if(timeClicked == 1) {
            Thread countingTo500ms = new Thread(countinga);
            countingTo500ms.start();
        }

        if(timeClicked == 2) { 
            String Receiver = roomPanel.getListHorsLigne().getSelectedValue();

            ContactHorsLigne1 c = null ;
            if(c == null) {   
                c = new ContactHorsLigne1(Receiver);
                 c.setSender(name);

                c.setVisible(true);

               
            } else {
                c.setVisible(true);
            }
        }
    }
    
    
    
    
    //============================================================================================================
    /*
    code labelRoomEvent() est utilisé pour passer d'une salle de discussion à une autre. 
    Il envoie une demande au serveur en utilisant le protocole de commande CMD_ROOM, suivi du nom de la nouvelle salle. 
    Le thread en cours est mis en veille pendant 200 millisecondes en utilisant la méthode Thread.sleep() pour attendre la réponse du serveur. 
    Ensuite, l'interface graphique est mise à jour pour afficher la nouvelle salle et cacher la salle précédente, en mettant à jour le titre de la fenêtre et l'étiquette de la salle. Le champ de texte où les messages sont saisis est effacé.
    */
    private void labelRoomEvent() {
        this.clientPanel.getTpMessage().setText("");
        this.sendToServer("CMD_ROOM|"+this.room);
        try {
            Thread.sleep(200);     
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.roomPanel.setVisible(false);
        this.clientPanel.setVisible(true);
        this.setTitle("\""+this.name+"\" - "+this.room);
        clientPanel.getLbRoom().setText(this.room);
    }
    
    /*
    Le deuxième code leaveRoom() est utilisé pour quitter une salle de discussion. 
    Il envoie une demande au serveur en utilisant le protocole de commande CMD_LEAVE_ROOM, suivi du nom de la salle actuelle. 
    Le thread en cours est mis en veille pendant 200 millisecondes en utilisant la méthode Thread.sleep() pour attendre la réponse du serveur. 
    Ensuite, l'interface graphique est mise à jour pour afficher la salle de discussion principale et cacher la salle de discussion actuelle, en mettant à jour le titre de la fenêtre et en effaçant le champ de texte où les messages sont saisis.
    */
    private void leaveRoom() {
        this.sendToServer("CMD_LEAVE_ROOM|"+this.room);
        try {
            Thread.sleep(200);     
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.roomPanel.setVisible(true);
        this.clientPanel.setVisible(false);
        //clear the textPane message:
        clientPanel.getTpMessage().setText("");
        this.setTitle("\""+this.name+"\"");
    }
   ///==============================================================================================================
    
    ////////////////////////Events////////////////////////////
    private void btOkEvent() {
        String hostname = loginPanel.getTfHost().getText().trim();
        nickname = loginPanel.getTfNickname().getText().trim();
        String pass = loginPanel.getTfPass().getText().trim();
        
        this.serverHost = hostname;
        this.name = nickname;
        
        if(hostname.equals("") || nickname.equals("") || pass.equals("")) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs !", "Attention!", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(!isConnectToServer) {
            isConnectToServer = true;   
            this.connectToServer(hostname); 
        }    
        this.sendToServer("CMD_CHECK_NAME|" +this.name+"|"+pass);       

        String response = this.recieveFromServer();
        if(response != null) {
            if (response.equals(NICKNAME_EXIST) || response.equals(NICKNAME_INVALID)) {
                JOptionPane.showMessageDialog(this, response, "Erreur", JOptionPane.ERROR_MESSAGE);
                
            } else {
               
                loginPanel.setVisible(false);
                roomPanel.setVisible(true);
                clientPanel.setVisible(false);
                ListMenu();
                this.setTitle("\""+name+"\"");

                menuBar.setVisible(true);

                
                clientThread = new Thread(this);
                clientThread.start();
                this.sendToServer("CMD_ROOM|"+this.room);     

                System.out.println("C'est : \""+name+"\"");
             
            }
        } else System.out.println("[btOkEvent()] Le serveur n'est pas encore lancé !");
    }
    
    
        public void ListMenu(){
    	 PublicKey publicKey = RSA.initFromStrings();
         privateKey = RSA.initFromStringsPrivate();
         this.sendToServer("CMD_key|" +rsa.encode(publicKey.getEncoded()));
        
//        roomPanel.onlineList_rp.addMouseListener(new MouseAdapter(){
//        @Override
//        public void mouseClicked(MouseEvent e) {
//        	
//        	  int index  = roomPanel.onlineList_rp.getSelectedIndex();
//              String s = (String) roomPanel.onlineList_rp.getSelectedValue();
//              final String selectedUser = roomPanel.onlineList_rp.getSelectedValue(); 
//             
//
//            
//            if(e.getButton() ==  MouseEvent.BUTTON1 && e.getClickCount() == 1 && index != -1){
//                JPopupMenu pop = new JPopupMenu();
//                JMenuItem menu1 = new JMenuItem("Add Friend");	        	                 
//                ActionListener menuListener = new ActionListener() {
//                public void actionPerformed(ActionEvent event) {
//                   if("Add Friend".equals(event.getActionCommand())){
//                        int opt = JOptionPane.showConfirmDialog(null, "Confirm Friend Request?","Confirmation",JOptionPane.YES_NO_OPTION);
//                        if(opt != JOptionPane.NO_OPTION){
//                            String sql = "INSERT INTO request(request,requestSender,requestReceiver,status)VALUES('Add Friend','"+name+"','"+selectedUser+"','pending')";
//                            try{  
//                                conn = new UserDatabase();
//
//								Statement stmt = conn.connect().createStatement();
//                                if(stmt.executeUpdate(sql) == 1){
//                                    JOptionPane.showMessageDialog(null, "Friend Request Sent");
//                                }
//                            
//                            }catch(Exception ex){
//                                System.out.println(ex);
//                            }
//                        }
//                    }
//                }
//                };
//                pop.add(menu1);
//                menu1.addActionListener(menuListener);
//                roomPanel.onlineList_rp.setComponentPopupMenu(pop);    
//            }   
//          }
//    });
        

    }
    
    private void btSignUpEvent() {
        String pass = this.signUpPanel.getTfPass().getText();
        String pass2 = this.signUpPanel.getTfPass2().getText();
        if(!pass.equals(pass2)) {
            JOptionPane.showMessageDialog(this, "Mots de passes différents", "Erreur", JOptionPane.ERROR_MESSAGE);
        } else {
            String nickname = signUpPanel.getTfID().getText().trim();
            String mail = signUpPanel.getmail().getText().trim();
            String hostName = signUpPanel.getTfHost().getText().trim();
            if(hostName.equals("") || nickname.equals("") || pass.equals("") || pass2.equals("") || mail.equals("")) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs", "Attention!", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if(!isConnectToServer) {
                isConnectToServer = true; 
                this.connectToServer(hostName); 
            }    
            this.sendToServer("CMD_SIGN_UP|" +nickname+"|"+pass+"|"+mail);      
        
            String response = this.recieveFromServer();
            if(response != null) {
                if(response.equals(NICKNAME_EXIST) || response.equals(ACCOUNT_EXIST)) {
                    JOptionPane.showMessageDialog(this, response, "Erreur", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, response+"\nVous pouvez vous connecter pour utiliser ChatApp !", "Inscription réussie!", JOptionPane.INFORMATION_MESSAGE);
                    signUpPanel.clearTf();
                }
            }
        }
        
    }
            
    private void btSendEvent() {
        String msg = null;
        String message = clientPanel.getTaInput().getText().trim();
        if(message.equals("")) clientPanel.getTaInput().setText("");
        else {
            try {
				 msg = AES.encrypt(message, aeskey);
                                 System.out.println("aes btSendEvent() "+aeskey);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            this.sendToServer("CMD_CHAT|" + msg);       
            this.btClearEvent();
        }
        
    }

    private void btClearEvent() {
        clientPanel.getTaInput().setText("");
    }

    private void btExitEvent() {
        try {
            isRunning = false;
            //this.disconnect();
            System.exit(0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
public void connectToServer(String hostAddress) {
    try {
        // SSL/TLS configuration
        String keystoreFile = "C:/Users/HP/Pictures/ChatApp/ChatApp/Keystore.jks";

        char[] keystorePassword = "123456789".toCharArray();

        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(new FileInputStream(keystoreFile), keystorePassword);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keystore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(hostAddress, 9999);
        
        // Rest of the code to initialize the socket and streams
        socketOfClient = sslSocket;
        bw = new BufferedWriter(new OutputStreamWriter(socketOfClient.getOutputStream()));
        br = new BufferedReader(new InputStreamReader(socketOfClient.getInputStream()));
        
    } catch (java.net.UnknownHostException e) {
        JOptionPane.showMessageDialog(this, "Adresse IP incorrecte.\nVeuillez essayer à nouveau!", "Impossible de se connecter au serveur", JOptionPane.ERROR_MESSAGE);
    } catch (java.net.ConnectException e) {
        JOptionPane.showMessageDialog(this, "Le serveur est inaccessible pour le moment.\nVeuillez essayer à nouveau!", "Impossible de se connecter au serveur", JOptionPane.ERROR_MESSAGE);
    } catch(java.net.NoRouteToHostException e) {
        JOptionPane.showMessageDialog(this, "Impossible de trouver cet utilisateur!\nVeuillez essayer à nouveau!", "Impossible de se connecter au serveur", JOptionPane.ERROR_MESSAGE);
    } catch (IOException | GeneralSecurityException ex) {
        Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
    }
}

    public void sendToServer(String line) {
        try {
            this.bw.write(line);
            this.bw.newLine(); 
            this.bw.flush();
        } catch (java.net.SocketException e) {
            JOptionPane.showMessageDialog(this, "Le serveur est arrêté, impossible d'envoyer un message!", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (java.lang.NullPointerException e) {
            System.out.println("[sendToServer()] Serveur non lancé !");
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String recieveFromServer() {
        try {
            return this.br.readLine();  
        } catch (java.lang.NullPointerException e) {
            System.out.println("[recieveFromServer()] Serveur non lancé !");
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void disconnect() {
        System.out.println("déconnecté()");
        try {
            if(br!=null) this.br.close();
            if(bw!=null) this.bw.close();
            if(socketOfClient!=null) this.socketOfClient.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        ClientFrame client = new ClientFrame(null);
        client.setVisible(true);
    }

    @Override
    public void run() {
        String response;
        String sender, receiver, fileName, thePersonIamChattingWith, thePersonSendFile;
        String msg= null;
        String cmd, icon;
        PrivateChat pc;
        
        while(isRunning) {
            try {
                response = this.recieveFromServer();
                tokenizer = new StringTokenizer(response, "|");
                cmd = tokenizer.nextToken();
                switch (cmd) {
                     case "CMD_Key":
            	String key = tokenizer.nextToken();
            	try {
					aeskey = RSA.decrypt(key,privateKey);
					               System.out.println("controller.ClientFrame.run()"+aeskey);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
                                        
				}
            	System.out.println("key from server :"+aeskey);
            	
            	break;
                    case "CMD_CHAT":
                        sender = tokenizer.nextToken();
                      String  msge = response.substring(cmd.length()+sender.length()+2, response.length());
                        try {
					msg = AES.decrypt(msge, aeskey);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

                        
                        if(sender.equals(this.name)) this.clientPanel.appendMessage(sender+": ", msg, Color.BLACK, new Color(0, 102, 204));
                        else this.clientPanel.appendMessage(sender+": ", msg, Color.MAGENTA, new Color(56, 224, 0));
                        
                        
                        break;
                        
                    case "CMD_PRIVATECHAT":
                        sender = tokenizer.nextToken();
                        String encrypt =  response.substring(cmd.length()+sender.length()+2, response.length());
                        try {
                                        System.out.println("##MESSAGE Recu CRYPTE## =>  "+encrypt);
					msg = AES.decrypt(encrypt, aeskey);
                                        System.out.println(aeskey);
                                        System.out.println("##MESSAGE DECRYPTE## =>  "+msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                        pc = listReceiver.get(sender);
                        
                        if(pc == null) {
                           // System.out.println("salam");
                            pc = new PrivateChat(name, sender, serverHost, bw, br,aeskey);
                            //pc.afficherMessageHorsLigne(name, sender);
                            pc.sender = name;
                            pc.receiver = sender;
                            pc.serverHost = this.serverHost;
                            pc.bw = ClientFrame.this.bw;
                            pc.br = ClientFrame.this.br;
                            
                            pc.getLbReceiver().setText(pc.receiver);
                            pc.setTitle(pc.receiver);
                            pc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            pc.setVisible(true);
                            
                            listReceiver.put(sender, pc);
                        } else {
                            System.out.println("wa3alikoum");
                            pc.setVisible(true);
                        }
                        pc.appendMessage_Left(sender+": ", msg);
                        break;
                        
                    case "CMD_ONLINE_USERS":
                        listModel.clear();
                        listModel_rp.clear();
                        listHorsLigne.clear();
                        while(tokenizer.hasMoreTokens()) {
                            cmd = tokenizer.nextToken();
                            listModel.addElement(cmd);
                            listModel_rp.addElement(cmd);
                        }
                        clientPanel.getOnlineList().setModel(listModel);
                        clientPanel.getOnlineList().setModel(listModel);
                        
                        listModel_rp.removeElement(this.name);
                        roomPanel.getOnlineList_rp().setModel(listModel_rp);
                        
                        //afficher les clients hors hors ligne
                        UserDatabase user = new UserDatabase();
                        user.connect();
                        ResultSet r= user.getData();
                        while(r.next()){
                            if(!listModel.contains( r.getString("name")))
                            {
                                listHorsLigne.addElement(r.getString("name"));
                            }
                        }
                        
                     
                        
                        roomPanel.getListHorsLigne().setModel(listHorsLigne);
                        
                        break;
                        
                    case "CMD_ONLINE_THIS_ROOM":
                        listModelThisRoom.clear();
                        while(tokenizer.hasMoreTokens()) {
                            cmd = tokenizer.nextToken();
                            listModelThisRoom.addElement(cmd);
                        }
                        clientPanel.getOnlineListThisRoom().setModel(listModelThisRoom);
                        break;
                        
                        
                        
                    case "CMD_FILEAVAILABLE":
                        System.out.println("fichier trouvé");
                        fileName = tokenizer.nextToken();
                        thePersonIamChattingWith = tokenizer.nextToken();
                        thePersonSendFile = tokenizer.nextToken();
                        
                        pc = listReceiver.get(thePersonIamChattingWith);
                        
                        if(pc == null) {
                            sender = this.name;
                            receiver = thePersonIamChattingWith;
                            pc = new PrivateChat(sender, receiver, serverHost, bw, br,aeskey);
                            
                            pc.getLbReceiver().setText("+pc.receiver+");
                            pc.setTitle(pc.receiver);
                            pc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            
                            listReceiver.put(receiver, pc);
                        }
                        
                        pc.setVisible(true);
                        pc.insertButton(thePersonSendFile, fileName);
                        break;
                        
                    case "CMD_ICON":
                        icon = tokenizer.nextToken();
                        cmd = tokenizer.nextToken();    //cmd = sender
                        
                        if(cmd.equals(this.name)) this.clientPanel.appendMessage(cmd+": ", "\n  ", Color.BLACK, Color.BLACK);
                        else this.clientPanel.appendMessage(cmd+": ", "\n   ", Color.MAGENTA, Color.MAGENTA);
                        
                        switch (icon) {
                            case "LIKE":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/like2.png")));
                                break;
                                
                            case "DISLIKE":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/dislike.png")));
                                break;
                                
                            case "PAC_MAN":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/pacman.png")));
                                break;
                                
                            case "SMILE":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/smile.png")));
                                break;
                                
                            case "GRIN":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/grin.png")));
                                break;
                                
                            case "CRY":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/cry.png")));
                                break;
                            case "ANGEL":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/angel.png")));
                                break;
                            case "ANGRY":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/angry.png")));
                                break;
                            case "SMILEE":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/big_smile.png")));
                                break;
                            case "CONSIDER":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/consider.png")));
                                break;
                            case "CUTE":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/cute.png")));
                                break;
                            case "HAHA":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/haha.png")));
                                break;
                            case "HEART":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/heart.png")));
                                break;
                            case "LOL":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/lol.png")));
                                break;
                            case "LOVE":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/love.png")));
                                break;
                            case "VOMISSEMENT":
                                this.clientPanel.getTpMessage().insertIcon(new ImageIcon(getClass().getResource("/images/vomissement.png")));
                                break;
                                
                                
                            default:
                                throw new AssertionError("The icon is invalid, or can't find icon!");
                        }
                        
                        break;
                        
                    default:
                        if(!response.startsWith("CMD_")) {
                            if(response.equals("Attention! Serveur non lancé!")) {
                                this.clientPanel.appendMessage(response, Color.RED);
                            }
                            else this.clientPanel.appendMessage(response, new Color(153, 153, 153));
                        }
                        
                        
                }
            } catch (SQLException ex) {
                Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Déconnecté du serveur");
    }


}
/*

*/