package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import java.io.Serializable;

public class User implements Serializable {
    private int userID;
    private int publicKey;

    public User(int id, int pk){
        userID = id;
        publicKey = pk;
    }

    public int getUserID() {
        return userID;
    }

    public int getPublicKey() {
        return publicKey;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setPublicKey(int publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public boolean equals(Object other){
        User user = (User) other;
        return user.getUserID() == this.userID;
    }
    

}