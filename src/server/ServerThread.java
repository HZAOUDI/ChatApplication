
package server;

import controller.aes;
import controller.rsa;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import javax.mail.internet.*; 
import java.util.Properties;  
import javax.crypto.SecretKey;
import javax.mail.*;  

public class ServerThread extends Thread {
    String encodedKey = null;
    Socket socketOfServer;
    BufferedWriter bw;
    BufferedReader br;
    SecretKey  aeskey;
    String clientName, clientPass, clientRoom, clientMail;
    public static Hashtable<String, ServerThread> listUser = new Hashtable<>();
    aes AES = new aes();
    public static final String NICKNAME_EXIST = "Cet email est déjà utilisé, veuillez entrer un email correct !";
    public static final String NICKNAME_VALID = "Email accepté";
    public static final String NICKNAME_INVALID = "Email ou nom d'utilisateur incorrecte";
    public static final String SIGNUP_SUCCESS = "Connexion réussie";
    public static final String ACCOUNT_EXIST = "Ce compte existe déjà !";
    
    public JTextArea taServer;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    
    StringTokenizer tokenizer;
    private final int BUFFER_SIZE = 1024;
    
    String senderName, receiverName;    
    static Socket senderSocket, receiverSocket;     
    
    UserDatabase userDB;
    
    static boolean isBusy = false;   // utilisé pour vérifier si le serveur envoie et reçoit des fichiers ou non
    
    
    public static void envoyermail(String adrs) {
            String mailscol = "noreplytestjava" ;
            String mdp = "123456789test";
            Properties p = new Properties();
            p.put("mail.smtp.ssl.trust", "smtp.gmail.com");
    	    p.put("mail.smtp.ssl.protocols", "TLSv1.2");
    	    p.put("mail.smtp.port", "587");
    	    p.put("mail.smtp.host", "smtp.gmail.com");
    	    p.put("mail.smtp.auth", "true");
    	    p.put("mail.smtp.starttls.enable","true");
    	    p.put("mail.smtp.auth", "true"); 
    	   
    	    
    	    p.put("mail.smtp.socketFactory.fallback", "false");

    	    Session s = Session.getDefaultInstance(p,
    	      new javax.mail.Authenticator() {
    	      protected PasswordAuthentication getPasswordAuthentication() {
    	        return new PasswordAuthentication(mailscol, mdp);
    	      }
    	    });   

    	    try {
    	      MimeMessage m = new MimeMessage(s);
    	      m.addRecipient(Message.RecipientType.TO,new InternetAddress(adrs));
    	      m.setSubject("Inscription à ChatApp");
    	      m.setText("Vous etes desormais inscrit à ChatApp");
    	      Transport.send(m);
    	      System.out.println("Message envoyé avec succès");
    	    } catch (MessagingException e) {
    	      e.printStackTrace();
    	    }
    	  
    	
    }
    
    
    public ServerThread(Socket socketOfServer) {
        this.socketOfServer = socketOfServer;
        this.bw = null;
        this.br = null;
        
        clientName = "";
        clientMail = "";
        clientPass = "";
        clientRoom = "";
        
        userDB = new UserDatabase();
        userDB.connect();
    }
    
    public void appendMessage(String message) { // définit la position du curseur juste après le texte inséré
        taServer.append(message);
        taServer.setCaretPosition(taServer.getText().length() - 1);   
    }
    
    // cette methode est utiliser pour récupérer les messages envoyés depuis le serveur parvenant d'un utilisateur donnée
    public String recieveFromClient() {
        try {
            return br.readLine();
        } catch (IOException ex) {
            System.out.println(clientName+" s'est déconnecté");
        }
        return null;
    }
    // cette methode est utilisée pour envoyer un message à un client spécifique 
    public void sendToClient(String response) { 
        try {
            bw.write(response);
            bw.newLine();
            bw.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // cette methode est utilisée pour envoyer un message à un utilisateur spécifique 
    // Elle prend en paramètre la socket du client ainsi que le message à envoyer
    public void sendToSpecificClient(ServerThread socketOfClient, String response) {  //n'envoie des messages qu'à des clients spécifiques
        try {
            BufferedWriter writer = socketOfClient.bw;
            writer.write(response);
            writer.newLine();
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendToSpecificClient(Socket socket, String response) {     
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(response);
            writer.newLine();
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void notifyToAllUsers(String message) {
        /*
        principe de fonctionnement : supposons que le client A envoie un message au serveur et que d'autres clients B, C et D se connectent également au serveur
        le premier serveur obtient socketOfClient dans listUser, socketOfClient obtient le nom correspondant A
        le serveur obtiendra le nom et le message du client A, puis enverra un message avec le contenu : "A : message" à tous les autres clients via
         via leur socketOfServers
         en résumé, le serveur envoie le message "A: message" à A,B,C,D via 4 sockets : socketOfServer de A, socketOfServer de B, socketOfServer de C, socketOfServer de D
         socketsOfServer stocké dans listUser
        */
        Enumeration<ServerThread> clients = listUser.elements();
        ServerThread st;
        BufferedWriter writer;
        
        while(clients.hasMoreElements()) {
            st = clients.nextElement();
            writer = st.bw;

            try {
                writer.write(message);
                writer.newLine();
                writer.flush();
            } catch (IOException ex) {
                Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //==================================================================================================================================== 
    /*La méthode "notifyToUsersInRoom(String message)" prend un seul argument "message" 
    qui est le message à envoyer à tous les utilisateurs connectés à la même "room" que l'expéditeur de la notification. 
    Elle utilise la variable "this.clientRoom" pour déterminer la chambre à laquelle appartient l'expéditeur de la notification. 
    Ensuite, elle utilise une boucle "while" pour parcourir tous les clients connectés au serveur et envoie le message aux clients dont la chambre correspond à celle de l'expéditeur.
    */
    public void notifyToUsersInRoom(String message) {
        Enumeration<ServerThread> clients = listUser.elements();
        ServerThread st;
        BufferedWriter writer;
        
        while(clients.hasMoreElements()) {
            st = clients.nextElement();
            if(st.clientRoom.equals(this.clientRoom)) { //envoie des messages aux personnes (st.clientRoom) dont la chambre correspond à la chambre de l'expéditeur (this.clientRoom) 
                writer = st.bw;

                try {
                    writer.write(message);
                    writer.newLine();
                    writer.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
 
    /*La méthode "notifyToUsersInRoom(String room, String message)" prend deux arguments : "room" qui représente la chambre à laquelle le message doit être envoyé, 
    et "message" qui est le message à envoyer. 
    Elle utilise la variable "room" pour déterminer la chambre à laquelle le message doit être envoyé. 
    Ensuite, elle utilise également une boucle "while" pour parcourir tous les clients connectés au serveur et envoie le message aux clients dont la chambre correspond à celle spécifiée dans le premier argument.
    */
    public void notifyToUsersInRoom(String room, String message) {  //envoie un message au room cad le client has just entred ou a quitter le groupe 
        Enumeration<ServerThread> clients = listUser.elements();
        ServerThread st;
        BufferedWriter writer;    
        while(clients.hasMoreElements()) {
            st = clients.nextElement();
            if(st.clientRoom.equals(room)) {
                writer = st.bw;

                try {
                    writer.write(message);
                    writer.newLine();
                    writer.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    /*la différence entre les deux méthodes est que la première envoie un message aux utilisateurs connectés à la même chambre que l'expéditeur de la notification, 
    tandis que la seconde envoie un message aux utilisateurs connectés à une chambre spécifiée en argument.
    */
    //====================================================================================================================================
    
    public void closeServerThread() {
        try {
            br.close();
            bw.close();
            socketOfServer.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getAllUsers() {
        StringBuffer kq = new StringBuffer();
        String temp = null;
        
        Enumeration<String> keys = listUser.keys();
        if(keys.hasMoreElements()) {
            String str = keys.nextElement();
            kq.append(str);
        }  
        while(keys.hasMoreElements()) {
            temp = keys.nextElement();
            kq.append("|").append(temp);
        } 
        return kq.toString();
    }
    public String getOnlineUsers() {
        StringBuffer kq = new StringBuffer();
        String temp = null;

        Enumeration<String> keys = listUser.keys();
        if (keys.hasMoreElements()) {
            String str = keys.nextElement();
            kq.append(str);
        }

        while (keys.hasMoreElements()) {
            temp = keys.nextElement();
            kq.append("|").append(temp);
        }

        return kq.toString();
    }
 
    //===================================================================================================================
    //renvoient tous deux une liste de noms d'utilisateurs connectés à une salle de discussion spécifique.  
        
    /*le code "getUsersThisRoom()" utilise la variable "this.clientRoom" 
        pour déterminer la salle de discussion à partir de laquelle la liste des utilisateurs doit être extraite, 
     tandis que le deuxième code "getUsersAtRoom(String room)" utilise une variable d'entrée "room" 
        pour spécifier la salle de discussion à partir de laquelle extraire la liste des utilisateurs.
     cad le premier code suppose que la salle de discussion doit être extraite à partir de la valeur "clientRoom" de l'objet courant, 
        tandis que le deuxième code permet de spécifier la salle de discussion en passant une variable d'entrée.   
    */
        
    public String getUsersThisRoom() {
        StringBuffer kq = new StringBuffer();
        String temp = null;
        ServerThread st;
        Enumeration<String> keys = listUser.keys();
        
        while(keys.hasMoreElements()) {
            temp = keys.nextElement();
            st = listUser.get(temp);
            if(st.clientRoom.equals(this.clientRoom))  kq.append("|").append(temp);
        }
        
        if(kq.equals("")) return "|";
        return kq.toString();   
    }
    
    public String getUsersAtRoom(String room) {
        StringBuffer kq = new StringBuffer();
        String temp = null;
        ServerThread st;
        Enumeration<String> keys = listUser.keys();
        
        while(keys.hasMoreElements()) {
            temp = keys.nextElement();
            st = listUser.get(temp);
            if(st.clientRoom.equals(room))  kq.append("|").append(temp);
        }
        
        if(kq.equals("")) return "|";
        return kq.toString();  
    }
    
    //==========================================================================================================================
    
    public void clientQuit() {
        /*
        lors de l'envoi du fichier, nous créerons un nouveau socket pour envoyer le fichier, et lors de l'envoi du fichier, le socket se fermera automatiquement
        parce que ce socket que nous avons créé n'a pas le nom du client, donc socket_that.clientName == null, donc pas besoin d'imprimer
        cette socket_information près de l'écran
        */

        if(clientName != null) {
            
            this.appendMessage("\n["+sdf.format(new Date())+"] Client \""+clientName+"\" s'est déconnecté");
            listUser.remove(clientName);
            if(listUser.isEmpty()) this.appendMessage("\n["+sdf.format(new Date())+"] Aucun utilisateur n'est connecté au serveur\n");
            notifyToAllUsers("CMD_ONLINE_USERS|"+getAllUsers());
            notifyToUsersInRoom("CMD_ONLINE_THIS_ROOM"+getUsersThisRoom());
            notifyToUsersInRoom(clientName+" a quitté l'application !");
        }
    }
    
    //=========================================================================================================================
    public void changeUserRoom() {  //permet à l'utilisateur courant de changer de salle de discussion. Pour cela, il met à jour la propriété "clientRoom" de l'objet "ServerThread" correspondant à l'utilisateur avec la nouvelle valeur de "this.clientRoom", puis met à jour la table de hachage "listUser" avec cet objet modifié.
        ServerThread st = listUser.get(this.clientName); //Récupère l'objet "ServerThread" correspondant à l'utilisateur actuel à partir de la table de hachage "listUser" en utilisant la clé "this.clientName".
        st.clientRoom = this.clientRoom;  //Met à jour la propriété "clientRoom" de l'objet "ServerThread" avec la nouvelle valeur de "this.clientRoom". Cela signifie que l'utilisateur actuel change de salle de discussion.
        listUser.put(this.clientName, st); //Met à jour la table de hachage "listUser" avec l'objet "ServerThread" modifié pour cet utilisateur, en utilisant à nouveau la clé "this.clientName".
    
        /*
         Notez que l'objet attaché au client demandant des changements de chambre est un objet de cette classe, donc la commande suivante suffit :
         suffisant pour remplacer les 3 commandes ci-dessus :
         listUser.put(clientName, this); // le deuxième paramètre est celui-ci, associé au client qui veut changer de pièce, et il a déjà changé de pièce, donc
         Cette commande met à jour la valeur avec key=clientName dans cette table de hachage
        */
        
        // cette méthode permet de changer la salle de discussion de l'utilisateur actuel en mettant à jour la propriété "clientRoom" de l'objet "ServerThread" correspondant à l'utilisateur avec la nouvelle valeur de "this.clientRoom", puis en mettant à jour la table de hachage "listUser" avec cet objet modifié.
    }
    
    public void removeUserRoom() {   // supprime l'utilisateur courant de la salle de discussion en mettant à jour la propriété "clientRoom" de l'objet "ServerThread" correspondant à l'utilisateur, puis en mettant à jour la table de hachage "listUser" avec cette nouvelle valeur.
        ServerThread st = listUser.get(this.clientName); //Récupère l'objet "ServerThread" correspondant à l'utilisateur actuel à partir de la table de hachage "listUser" en utilisant la clé "this.clientName".
        st.clientRoom = this.clientRoom; //Met à jour la propriété "clientRoom" de l'objet "ServerThread" avec la valeur actuelle de "this.clientRoom". Cela signifie que l'utilisateur actuel est retiré de sa salle de discussion actuelle.
        listUser.put(this.clientName, st); //Met à jour la table de hachage "listUser" avec la valeur mise à jour de l'objet "ServerThread" pour cet utilisateur, en utilisant à nouveau la clé "this.clientName".
        
        // Similaire à la fonction ci-dessus, cette commande suffit : listUser.put(clientName, this);
    }  // cette méthode permet de retirer l'utilisateur courant de la salle de discussion actuelle en mettant à jour la propriété "clientRoom" de l'objet "ServerThread" correspondant à l'utilisateur, puis en mettant à jour la table de hachage "listUser" avec cette nouvelle valeur.
    //===========================================================================================================================
    
    
    @Override
    public void run() {
        try {
            //créer des flux d'entrée et de sortie avec le socket client
            bw = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            
            boolean isUserExist = true;
            String message, sender, receiver, fileName;
            StringBuffer str;
            String cmd, icon;
            while(true) {  //attend que le client envoie un message et réponde
                try {
                    message = recieveFromClient();
                    tokenizer = new StringTokenizer(message, "|");
                    cmd = tokenizer.nextToken();
                    
                    switch (cmd) {
                        case "CMD_key":
                    	rsa RSA = new rsa();
                        String rsakey = tokenizer.nextToken();
                        
                        byte[] publicKeyBytes = Base64.getDecoder().decode(rsakey);
                        KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
                       PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
                       
                        
      
                        aeskey = AES.setKey("key"); // generate aes key
                      
                       encodedKey=   (String) Base64.getEncoder().encodeToString(aeskey.getEncoded());
                       System.out.println( "aes server encodedKey : " + encodedKey);

                       String encryptedKEY = RSA.encrypt(encodedKey,publicKey);
                       System.err.println("key from server :" + encryptedKEY);
                     
                       
                        this.sendToClient("CMD_Key|" +encryptedKEY);
                       break;
                        case "CMD_CHAT":
                            str = new StringBuffer(message);
                            str = str.delete(0, 9);
                            String  messa = new String(AES.decrypt(str.toString(), encodedKey));
                            String messageEnc = AES.encrypt(messa, encodedKey);
                            System.out.println("message crypte"+messageEnc);
                            notifyToUsersInRoom("CMD_CHAT|" + this.clientName+"|"+messageEnc);  //this.clientName = le nom du client auquel envoyer le message  
                            
                            break;
                            
                        case "CMD_PRIVATECHAT":
                              String emailReceiver="";
                            String privateSender = tokenizer.nextToken();
                            String privateReceiver = tokenizer.nextToken();
                            String messag = message.substring(cmd.length()+privateSender.length()+privateReceiver.length()+3, message.length());
//                            System.out.println("originalkeyserver :" +aeskey);
                           String messageContent =new String(AES.decrypt(messag,encodedKey));
                          
                        
                            //ServerThread st_sender = listUser.get(privateSender);
                            ServerThread st_receiver = listUser.get(privateReceiver);
                            String messageEn = AES.encrypt(messageContent,encodedKey );
                            System.out.println("##MESSAGE CRYPTE## =>  "+messageEn);
                            //sendToSpecificClient(st_sender, "CMD_PRIVATECHAT|" + privateSender + "|" + messageContent);
                            sendToSpecificClient(st_receiver, "CMD_PRIVATECHAT|" + privateSender + "|" + messageEn);
                           
                            break;
                            
                        case "CMD_ROOM": /*Cela signifie que le client veut rejoindre une salle de discussion spécifique. 
                                        La chaîne suivante est un jeton du nom de la salle que le client souhaite rejoindre. 
                                        Ensuite, la méthode "changeUserRoom()" est appelée pour changer la salle de discussion de l'utilisateur courant. 
                                        Ensuite, plusieurs notifications sont envoyées à tous les utilisateurs du réseau, ainsi qu'aux utilisateurs de la salle de discussion à laquelle l'utilisateur vient de se joindre. 
                                        Cela inclut une notification pour mettre à jour la liste de tous les utilisateurs, une notification pour mettre à jour la liste des utilisateurs en ligne, une notification pour mettre à jour la liste des utilisateurs en ligne dans la salle de discussion et une notification pour informer les utilisateurs de la salle de discussion que l'utilisateur actuel vient de rejoindre.*/
                            clientRoom = tokenizer.nextToken();
                            changeUserRoom();
                            notifyToAllUsers("CMD_USERS|" + getAllUsers());
                            notifyToAllUsers("CMD_ONLINE_USERS|"+getAllUsers());
                            notifyToUsersInRoom("CMD_ONLINE_THIS_ROOM"+getUsersThisRoom());
                            notifyToUsersInRoom(clientName+" has just entered!");
                            break;
                            
                        case "CMD_LEAVE_ROOM": /* Cela signifie que l'utilisateur courant souhaite quitter la salle de discussion actuelle. 
                                                La variable "room" est initialisée avec le nom de la salle de discussion actuelle de l'utilisateur. 
                                                Ensuite, la propriété "clientRoom" de l'utilisateur est réinitialisée à une chaîne vide pour indiquer que l'utilisateur n'appartient plus à une salle de discussion. 
                                                Ensuite, la méthode "removeUserRoom()" est appelée pour retirer l'utilisateur de la salle de discussion actuelle. 
                                                Enfin, des notifications sont envoyées à tous les utilisateurs de la salle de discussion pour mettre à jour les listes des utilisateurs en ligne et pour informer les autres utilisateurs de la salle de discussion que l'utilisateur actuel vient de quitter.*/
                            String room = clientRoom;
                            clientRoom = "";  //si clientRoom = null, erreur !  
                            removeUserRoom();
                            notifyToUsersInRoom(room, "CMD_ONLINE_THIS_ROOM"+getUsersAtRoom(room));
                            notifyToUsersInRoom(room, clientName+" a quitté le groupe"); //note d'utiliser cette commande avant clientRoom = "" ; car si clientRoom = "" alors sachez à quelle salle envoyer le message :v     
                            
                            break;
                            
                        case "CMD_CHECK_NAME":
                            clientName = tokenizer.nextToken();
                            clientPass = tokenizer.nextToken();
                            isUserExist = listUser.containsKey(clientName);
                            
                            if(isUserExist) {  // le surnom existe, cela signifie que quelqu'un d'autre est connecté avec ce surnom
                                sendToClient(NICKNAME_EXIST);
                            }
                            else {  //surnom toujours personne connecté
                                int kq = userDB.checkUser(clientName, clientPass);
                                if(kq == 1) {
                                    sendToClient(NICKNAME_VALID);
                                 // puis si le nom est valide, placez ce pseudo dans la table de hachage et discutez avec le client :
                                    this.appendMessage("\n["+sdf.format(new Date())+"] Client \""+clientName+"\" s'est connecté au serveur");
                                    listUser.put(clientName, this);    //ajoutez le nom de cet objet et ajoutez cet objet à listUser
                                } else sendToClient(NICKNAME_INVALID);
                            }
                            break;
                            
                        case "CMD_SIGN_UP":
                            String name = tokenizer.nextToken();
                            String pass = tokenizer.nextToken();
                            String mail = tokenizer.nextToken();
                            //clientMail = mail ;
                            System.out.println("nom d utilisateur, mot de passe, mail = "+name+" - "+pass+" - "+mail);
                            isUserExist = listUser.containsKey(name);
                            System.out.println("mot de passe = "+pass);
                            if(isUserExist) {
                                sendToClient(NICKNAME_EXIST);
                            } else {
                                System.out.println("mot de passe = "+pass);
                                System.out.println("weeeeeeeeeeeeee");
                                ServerThread.envoyermail(mail);
                                System.out.println("mot de passe = "+pass);
                                int kq = userDB.insertUser(new User(name, pass, mail));
                                if(kq > 0) {
                                    sendToClient(SIGNUP_SUCCESS);
                                } else sendToClient(ACCOUNT_EXIST);
                            }
                            break;
                            
                        case "CMD_ONLINE_USERS":
                            notifyToAllUsers("CMD_USERS|" + getAllUsers());
                            notifyToAllUsers("CMD_ONLINE_USERS|" + getOnlineUsers());
                            sendToClient("CMD_ONLINE_USERS|"+getAllUsers());
                            notifyToUsersInRoom("CMD_ONLINE_THIS_ROOM"+getUsersThisRoom());
                            break;
                        
                        case "CMD_SENDFILETOSERVER":  //the sender sends a file to server:
                            sender = tokenizer.nextToken();
                            receiver = tokenizer.nextToken();
                            fileName = tokenizer.nextToken();
                            int len = Integer.valueOf(tokenizer.nextToken());
                            
                            String path = System.getProperty("user.dir") + "\\sendfile\\" +fileName;
                            

                            BufferedInputStream bis = new BufferedInputStream(socketOfServer.getInputStream());  // récupère le flux d'entrée de sender 
                            FileOutputStream fos = new FileOutputStream(path); //la sortie est vers le fichier qui sera stocké sur le disque dur du serveur  
                            
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int count = -1;
                            while((count = bis.read(buffer)) > 0) {  // est lu le nombre de mots que l'expéditeur stockera temporairement dans le tableau tampon
                                fos.write(buffer, 0, count);  // puis os prend le tampon et l'envoie au récepteur       
                            }

                            Thread.sleep(300);
                            bis.close();
                            fos.close();
                            socketOfServer.close();
                      
                            ///informe l'expéditeur et le destinataire que le fichier vient d'être uploadé, puis ils veulent le télécharger, c'est leur affaire :
                            ServerThread stSender = listUser.get(sender);
                            /*
                            notez que stSender n'est pas socketOfServer ci-dessus,
                            car socketOfServer est une socket connectée à une socket temporaire de l'expéditeur. Ce socket temporaire est créé lorsque l'expéditeur le souhaite
                            envoie un fichier au serveur, et après l'envoi du fichier, le socket temporaire disparaît
                            */
                            
                            ServerThread stReceiver = listUser.get(receiver);
                            
                            sendToSpecificClient(stSender, "CMD_FILEAVAILABLE|"+fileName+"|"+receiver+"|"+sender);
                            sendToSpecificClient(stReceiver, "CMD_FILEAVAILABLE|"+fileName+"|"+sender+"|"+sender);
                            
                            isBusy = false;
                            break;
                            
                        case "CMD_DOWNLOADFILE":  //server sends file to someone who just pressed download file  
                            fileName = tokenizer.nextToken();
                            path = System.getProperty("user.dir") + "\\sendfile\\" + fileName;
                            FileInputStream fis = new FileInputStream(path);
                            BufferedOutputStream bos = new BufferedOutputStream(socketOfServer.getOutputStream());
                            
                            byte []buffer2 = new byte[BUFFER_SIZE];
                            int count2=0;
                            
                            while((count2 = fis.read(buffer2)) > 0) {
                                bos.write(buffer2, 0, count2); // Envoie en continu chaque partie du fichier au serveur
                            }

                            bos.close();
                            fis.close();
                            socketOfServer.close();
                            
                            break;
                            
                        case "CMD_ICON": // La chaîne suivante est un jeton représentant le chemin d'accès à l'icône. Ensuite, une notification est envoyée à tous les utilisateurs de la salle de discussion actuelle pour mettre à jour l'icône de profil de l'utilisateur actuel. La notification inclut une commande "CMD_ICON" suivie du chemin d'accès à l'icône et du nom de l'utilisateur.
                            icon = tokenizer.nextToken();
                            notifyToUsersInRoom("CMD_ICON|"+icon+"|"+this.clientName);
                            break;
                            
                        default: //Si la commande entrée par l'utilisateur ne correspond à aucune des commandes prédéfinies, elle sera considérée comme un message de discussion normal. Dans ce cas, une notification est envoyée à tous les utilisateurs de la salle de discussion actuelle pour afficher le message de discussion
                            notifyToAllUsers(message);
                            break;
                    }
                    
                } catch (Exception e) {
                    clientQuit();
                    break;
                }
            }
        } catch (IOException ex) {
            clientQuit();
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
  
    }
}