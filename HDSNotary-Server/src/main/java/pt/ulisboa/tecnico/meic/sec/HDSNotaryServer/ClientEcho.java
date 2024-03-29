package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.util.Interaction;

import static java.util.Collections.frequency;
import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.NotaryService.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.*;

/**
 * Class for storing properties of Authenticated Double-Echo Broadcast per
 * notary
 */
public class ClientEcho {
    private ArrayList<Interaction> echos;
    private ArrayList<Interaction> readys;
    private boolean sentEcho;
    private boolean sentReady;
    private boolean delivered;
    private Interaction quorumEchos;
    private Interaction quorumReadys;
    private final Lock deliveredLock = new ReentrantLock();
    private Condition deliveredCondition = deliveredLock.newCondition();

    public ClientEcho() {
        this.clean();
    }

    public Condition getDeliveredCondition() {
        return deliveredCondition;
    }

    public void setDeliveredCondition(Condition deliveredCondition) {
        this.deliveredCondition = deliveredCondition;
    }

    public Lock getDeliveredLock() {
        return this.deliveredLock;
    }

    public ArrayList<Interaction> getEchos() {
        synchronized(this.echos) {
            return echos;
        }
    }

    public void setEchos(ArrayList<Interaction> echos) {
        synchronized(this.echos) {
            this.echos = echos;
        }
    }

    public ArrayList<Interaction> getReadys() {
        synchronized(this.readys) {
            return this.readys;
        }
    }

    public void setReadys(ArrayList<Interaction> readys) {
        synchronized(this.readys) {
            this.readys = readys;
        }
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

    public void addEcho(Interaction echo) {
        synchronized(this.echos) {
            this.echos.add(echo);
        }
    }

    public void addReady(Interaction echo) {
        synchronized(this.readys) {
            this.readys.add(echo);
        }
    }

    public Interaction getQuorumReadys() {
        synchronized(this.quorumReadys) {
            return quorumReadys;
        }
    }

    public void setQuorumReadys(Interaction quorumReadys) {
        synchronized(this.quorumReadys) {
            this.quorumReadys = quorumReadys;
        }
    }

    public Interaction getQuorumEchos() {
        synchronized(this.quorumEchos) {
            return quorumEchos;
        }
    }

    public void setQuorumEchos(Interaction quorumEchos) {
        synchronized(this.quorumEchos) {
            this.quorumEchos = quorumEchos;
        }
    }

    public int getNumberOfQuorumReceivedEchos() {
        synchronized(this.echos) {
            int maxEchos = 0;
            for (Interaction interaction : this.echos) {
                if (interaction != null) {
                    int numberOfInteractions = frequency(this.echos, interaction);
                    if (numberOfInteractions > maxEchos) {
                        maxEchos = numberOfInteractions;
                        if(maxEchos > ((NUMBER_OF_NOTARIES + F)/2)) {
                                setQuorumEchos(interaction);
                                System.out.println(" getNumberOfQuorumReceivedEchos Escrevi no QuorumsEchos: " + interaction.toString());
                                break;
                        }

                    }
                }
            }
            return maxEchos;
        }
    }

    public int getNumberOfQuorumReceivedReadys() {
        synchronized(this.readys) {
            int maxReadys = 0;
            for (Interaction interaction : this.readys) {
                if (interaction != null) {
                    int numberOfInteractions = frequency(this.readys, interaction);
                    if (numberOfInteractions > maxReadys) {
                        maxReadys=numberOfInteractions;
                        if(maxReadys > (2*F)) {
                                setQuorumReadys(interaction);
                                System.out.println(" getNumberOfQuorumReceivedReadys Escrevi no QuorumsReadys: " + interaction.toString());
                                break;
                        }
                    }
                }
            } 
            return maxReadys;
        }
    } 
    
    public void clean() {
        this.echos = new ArrayList<>();
        this.readys = new ArrayList<>();
        this.sentEcho = false;
        this.sentReady = false;
        this.delivered = false;
        this.quorumEchos = new Interaction();
        this.quorumReadys = new Interaction();
    }

}
