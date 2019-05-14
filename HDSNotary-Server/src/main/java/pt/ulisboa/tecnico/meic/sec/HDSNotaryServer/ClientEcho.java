package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.util.Interaction;

import static java.util.Collections.frequency;
import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.NotaryService.NUMBER_OF_NOTARIES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.*;

/**
 * Class for storing properties of Authenticated Double-Echo Broadcast per
 * notary
 */
public class ClientEcho {
    private int clientID;
    private Interaction[] echos;
    private Interaction[] readys;
    private boolean sentEcho;
    private boolean sentReady;
    private boolean delivered;
    private final Lock lock = new ReentrantLock();
    private Condition quorumEchos = lock.newCondition();
    private Condition quorumReadys = lock.newCondition();
    private Lock deliveredLock = new ReentrantLock();
    private Condition safetyDelivered = deliveredLock.newCondition();
    private Interaction quorum;

    public ClientEcho(int clientID) {
        this.clientID = clientID;
        this.clean();
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public Interaction[] getEchos() {
        return echos;
    }

    public void setEchos(Interaction[] echos) {
        this.echos = echos;
    }

    public Interaction[] getReadys() {
        return readys;
    }

    public void setReadys(Interaction[] readys) {
        this.readys = readys;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isSentReady() {
        return sentReady;
    }

    public void setSentReady(boolean sentReady) {
        synchronized(this) {
            this.sentReady = sentReady;
        }
    }

    public boolean isSentEcho() {
        return sentEcho;
    }

    public void setSentEcho(boolean sentEcho) {
        synchronized(this) {
            this.sentEcho = sentEcho;
        }
    }

    public void addEcho(int position, Interaction echo) {
        this.echos[position] = echo;
    }

    public void addReady(int position, Interaction echo) {
        this.readys[position] = echo;
    }

    public Condition getQuorumReadys() {
        return quorumReadys;
    }

    public void setQuorumReadys(Condition quorumReadys) {
        this.quorumReadys = quorumReadys;
    }

    public Condition getQuorumEchos() {
        return quorumEchos;
    }

    public void setQuorumEchos(Condition quorumEchos) {
        this.quorumEchos = quorumEchos;
    }

    public Interaction getQuorum() {
        return quorum;
    }

    public void setQuorum(Interaction quorum) {
        this.quorum = quorum;
    }

    public Lock getLock() {
        return this.lock;
    }

    public Lock getDeliveredLock() {
        return this.deliveredLock;
    }

    public int getNumberOfQuorumReceivedEchos() {
        int echos = 0;
        for (Interaction interaction : this.echos) {
            if (interaction != null) {
                int numberOfInteractions = frequency(Arrays.asList(this.echos), interaction);
                if (numberOfInteractions > echos) {
                    setQuorum(interaction);
                    echos = numberOfInteractions;
                }
            }
        }
        return echos;
    }

    public int getNumberOfQuorumReceivedReadys() {
        int readys = 0;
        for (Interaction interaction : this.readys) {
            if (interaction != null) {
                int numberOfInteractions = frequency(Arrays.asList(this.readys), interaction);
                if (numberOfInteractions > readys) {
                    setQuorum(interaction);
                    readys=numberOfInteractions;
                } 
            }
        } 
        return readys;
    } 
    
    public void clean() {
        //System.out.println("varejeira do clean!!!");
        this.echos = new Interaction[NUMBER_OF_NOTARIES + 1];
        this.readys = new Interaction[NUMBER_OF_NOTARIES + 1];
        this.sentEcho = false;
        this.sentReady = false;
        this.delivered = false;
        this.quorum = null;
    }

}
