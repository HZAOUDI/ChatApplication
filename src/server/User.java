
package server;

public class User {
    String name;
    String pass;
    String mail;
    private String room;

    public User(String name, String pass , String mail) {
        this.name = name;
        this.pass = pass;
        this.mail = mail;
    }

    public User(String name) {
        this.name = name;
    }

    public String getRoom() {
        return room;
    }

    
    public void setRoom(String room) {
        this.room = room;
    }
    
    
}