package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import java.io.Serializable;
import java.security.PublicKey;

public class User implements Serializable {
    private int userID;
    private PublicKey publicKey;
    private int clock;

    public User(int id, PublicKey pk){
        userID = id;
        publicKey = pk;
        clock = 0;
    }

    public int getUserID() {
        return userID;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public int getClock() {
        return clock;
    }

    public void setClock(int clock) {
        this.clock = clock;
    }

    @Override
    public boolean equals(Object other){
        User user = (User) other;
        return user.getUserID() == this.userID;
    }
    

}