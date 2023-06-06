
package server;




import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.net.ssl.*;
import java.security.*;
import java.io.FileInputStream;



public class ServerFrame extends JFrame implements Runnable {    
    
private SSLServerSocketFactory sslServerSocketFactory;
private SSLServerSocket sslServerSocket;
    
    JButton btStart, btStop;
    JTextArea taInfo;
    ServerSocket serverSocket;
    
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    ServerThread serverThread;
    
    public ServerFrame() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize((new Dimension(837, 600)));
        
        JLabel lbStateServer = new JLabel("Infos sur le serveur\n");
        lbStateServer.setFont(new java.awt.Font("Costantia", 1, 24));
        lbStateServer.setForeground(new java.awt.Color(204,0,204));
        lbStateServer.setBackground(new java.awt.Color(255, 255, 255));
        
        taInfo = new JTextArea();
        taInfo.setEditable(false);
        taInfo.setFont(new java.awt.Font("Costantia", 0, 16));
        taInfo.setBackground(new java.awt.Color(255, 255, 255)); 
        taInfo.setForeground(new java.awt.Color(0, 0, 0));  
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(taInfo);
        scroll.setPreferredSize(new Dimension(837, 600));
        
        btStart = new JButton("Lancer Serveur");
        btStart.setBackground(new java.awt.Color(204,0,204));
        btStart.setForeground(new java.awt.Color(204,0,204));
        btStart.setFont(new java.awt.Font("Costantia", 0, 12));
        btStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                btStartEvent(ae);
            }
        });
        
        btStop = new JButton("Quitter");
        btStop.setBackground(new java.awt.Color(204,0,204));
        btStop.setForeground(new java.awt.Color(204,0,204));
        btStop.setFont(new java.awt.Font("Costantia", 0, 12));
        btStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                btStopEvent(ae);
            }
        });
        btStart.setEnabled(true);
        btStop.setEnabled(false);
        
        JPanel panelBtn = new JPanel();
        panelBtn.add(btStart);
        panelBtn.add(btStop);
        
        JPanel p1 = new JPanel();
        p1.setPreferredSize(new Dimension(30, 30));
        JPanel p2 = new JPanel();
        p2.setPreferredSize(new Dimension(30, 30));
        
        panel.add(lbStateServer, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelBtn, BorderLayout.SOUTH);
        panel.add(p1, BorderLayout.WEST);
        panel.add(p2, BorderLayout.EAST);
        
        
        this.add(panel);
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void appendMessage(String message) {
        taInfo.append(message);
        taInfo.setCaretPosition(taInfo.getText().length() - 1);
    }
    
private void startServer() {
    try {
        // Chargement du certificat du keystore
        String keystoreFile = "C:/Users/HP/Pictures/ChatApp/ChatApp/Keystore.jks";

        char[] keystorePassword = "123456789".toCharArray();

        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(new FileInputStream(keystoreFile), keystorePassword);

        // Configuration du gestionnaire de clés du serveur avec le certificat
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, keystorePassword);

        // Configuration du contexte SSL/TLS
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        // Création du serveur socket sécurisé
        sslServerSocketFactory = sslContext.getServerSocketFactory();
        sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(9999);

        appendMessage("["+sdf.format(new Date())+"] Le serveur est prêt !");
        appendMessage("\n["+sdf.format(new Date())+"] Aucun utilisateur n'est connecté\n");

        while(true) {
            Socket socketOfServer = sslServerSocket.accept();
            serverThread = new ServerThread(socketOfServer);
            serverThread.taServer = this.taInfo;
            serverThread.start();
        }

    } catch (java.net.SocketException e) {
        System.out.println("Le serveur est arrêté");
        System.exit(0);
    } catch (IOException ex) {
        Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        System.out.println("Erreur ! Le port est occupé !");
        System.out.println("Le serveur est occupé ! ");
        JOptionPane.showMessageDialog(this, "Ce port est occupé par le serveur", "Erreur", JOptionPane.ERROR_MESSAGE);
        this.setVisible(false);
        System.exit(0);
    } catch (Exception ex) {
        Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        System.out.println("Erreur lors de la configuration SSL/TLS !");
        JOptionPane.showMessageDialog(this, "Erreur lors de la configuration SSL/TLS", "Erreur", JOptionPane.ERROR_MESSAGE);
        this.setVisible(false);
        System.exit(0);
    }
}


private void btStartEvent(ActionEvent ae) {
    Connection conn = new UserDatabase().connect();
    if(conn == null) {
        JOptionPane.showMessageDialog(this, "Veuillez lancer la base de données !", "Erreur!", JOptionPane.ERROR_MESSAGE);
        return;
    }
    new Thread(this).start();
    this.btStart.setEnabled(false);
    this.btStop.setEnabled(true);
}

    
    private void btStopEvent(ActionEvent ae) {
        int kq = JOptionPane.showConfirmDialog(this, "Voulez-vous vraiment arrêter le serveur", "Alerte", JOptionPane.YES_NO_OPTION);
        if(kq == JOptionPane.YES_OPTION) {
            try {
                 //notify to all clients that server is closed:
                serverThread.notifyToAllUsers("Attention: Le serveur est arrêté !");
                
                //then close server:
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        ServerFrame serverFrame = new ServerFrame();
        serverFrame.setVisible(true);
    }

    @Override
    public void run() {
        this.startServer();
    }
}