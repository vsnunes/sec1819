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
    private int clientID;
    private Interaction[] echos;
    private Interaction[] readys;
    private boolean sentEcho;
    private boolean sentReady;
    private boolean delivered;
    private Interaction quorumEchos;
    private Interaction quorumReadys;

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
        synchronized(this.echos) {
            return echos;
        }
    }

    public void setEchos(Interaction[] echos) {
        synchronized(this.echos) {
            this.echos = echos;
        }
    }

    public Interaction[] getReadys() {
        synchronized(this.readys) {
            return this.readys;
        }
    }

    public void setReadys(Interaction[] readys) {
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

    public void addEcho(int position, Interaction echo) {
        synchronized(this.echos){
            this.echos[position] = echo;
        }
    }

    public void addReady(int position, Interaction echo) {
        synchronized(this.readys) {
            this.readys[position] = echo;
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
                    int numberOfInteractions = frequency(Arrays.asList(this.echos), interaction);
                    if (numberOfInteractions > maxEchos) {
                        maxEchos = numberOfInteractions;
                        if(maxEchos > ((NUMBER_OF_NOTARIES + F)/2)) {
                            //if(quorumEchos == null) {
                                setQuorumEchos(interaction);
                                System.out.println(" getNumberOfQuorumReceivedEchos Escrevi no QuorumsEchos: " + interaction.toString());
                            /*}
                            else {
                                System.out.println(" getNumberOfQuorumReceivedEchos quorumEchos e null" + maxEchos);
                            }*/
                        }
                        else {
                            //System.out.println(" getNumberOfQuorumReceivedEchos nao entrei no if da verificacao do quorum" + maxEchos);
                        }
                    } 
                    else {
                        //System.out.println(" getNumberOfQuorumReceivedEchos nao escrevi" + maxEchos);
                    }
                }
            }
            return maxEchos;
        }
    }

    public int getNumberOfQuorumReceivedReadys() {
        synchronized(this.readys) {
            int maxReadys = 0;
            System.out.println("Tenho " + this.readys.length + " readys!");
            for (Interaction interaction : this.readys) {
                if (interaction != null) {
                    int numberOfInteractions = frequency(Arrays.asList(this.readys), interaction);
                    System.out.println(" getNumberOfQuorumReceivedReadys #interactions " + numberOfInteractions + " maxReadys: " + maxReadys);
                    if (numberOfInteractions > maxReadys) {
                        maxReadys=numberOfInteractions;
                        if(maxReadys > (2*F)) {
                            //if(quorumReadys == null) {
                                setQuorumReadys(interaction);
                                System.out.println(" getNumberOfQuorumReceivedReadys Escrevi no QuorumsReadys: " + interaction.toString());
                            /*}
                            else {
                                System.out.println(" getNumberOfQuorumReceivedReadys quorumReadys e null" + maxReadys);
                            }*/
                        }
                        else {
                            //System.out.println(" getNumberOfQuorumReceivedReadys nao entrei no if da verificacao do quorum" + maxReadys);
                        }
                    } 
                    else {
                        //System.out.println(" getNumberOfQuorumReceivedReadys nao escrevi" + maxReadys);
                    }
                }
            } 
            return maxReadys;
        }
    } 
    
    public void clean() {
        System.out.println("varejeira do clean!!!");
        this.echos = new Interaction[NUMBER_OF_NOTARIES + 1];
        this.readys = new Interaction[NUMBER_OF_NOTARIES + 1];
        this.sentEcho = false;
        this.sentReady = false;
        this.delivered = false;
        this.quorumEchos = new Interaction();
        this.quorumReadys = new Interaction();
    }

}
