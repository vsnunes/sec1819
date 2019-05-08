package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.util.Interaction;

import java.util.ArrayList;

public class ClientEcho {
    private final int NUMBER_OF_NOTARIES = 4;
    private int clientID;
    private ArrayList<Interaction> echos;
    private Interaction pending;

    public ClientEcho(int clientID) {
        this.clientID = clientID;
        this.echos = new ArrayList<Interaction>(NUMBER_OF_NOTARIES);
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public ArrayList<Interaction> getEchos() {
        return echos;
    }

    public void setEchos(ArrayList<Interaction> echos) {
        this.echos = echos;
    }

    public Interaction getPending() {
        return pending;
    }

    public void setPending(Interaction pending) {
        this.pending = pending;
    }
}
