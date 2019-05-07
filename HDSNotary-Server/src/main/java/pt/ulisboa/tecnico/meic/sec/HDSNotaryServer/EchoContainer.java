package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import java.util.ArrayList;

import pt.ulisboa.tecnico.meic.sec.util.Interaction;

public class EchoContainer {

    public static int EchoID = 1;

    /** The ID of the Echo */
    private int id;

    /** List of echos received for this */
    private ArrayList<Interaction> echos;
    
    private boolean sentEcho;

    private boolean delivered;

    /** The author of this echo */
    private int notaryID;

    public EchoContainer(int id) {
        this.id = id;
        this.echos = new ArrayList<>();
        this.sentEcho = false;
        this.delivered = false;
    }

    public int getID() {
        return this.id;
    }

    public void addEcho(Interaction interaction) {
        this.echos.add(interaction);
    }

    public ArrayList<Interaction> getEchos() {
        return this.echos;
    }

    public boolean isSentEcho() {
        return this.sentEcho;
    }

    public boolean isDelivered() {
        return this.delivered;
    }

    public void setNotaryID(int notaryID) {
        this.notaryID = notaryID;
    }

    public int getNotaryID() {
        return this.notaryID;
    }

}