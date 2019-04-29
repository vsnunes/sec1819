package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import javafx.util.Pair;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A class for describing Good items
 */
public class Good implements Serializable {

    /** The good identifier **/
    private int goodID;

    private boolean forSell;

    private User owner;

    private int timeStamp;

    private int writeTimeStamp;

    private ArrayList<Pair<Integer,Boolean>> readList;

    private byte[] sigma;

    /** Prevents the good from having concurrent transactions **/
    private boolean inTransaction;

    Good(int goodID, User owner) throws GoodException {
        checkArguments(goodID, owner);
        this.goodID = goodID;
        this.forSell = false;
        this.owner = owner;
        this.inTransaction = false;
        this.initialize();
    }

    Good(int goodID, User owner, boolean forSell) throws GoodException {
        checkArguments(goodID, owner);
        this.goodID = goodID;
        this.forSell = forSell;
        this.owner = owner;
        this.inTransaction = false;
        this.initialize();
    }

    public void setGoodID(int goodID) throws GoodException{
        checkArguments(goodID, this.getOwner());
        this.goodID = goodID;
    }

    public User getOwner() {
        return this.owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public int getGoodID() {
        return this.goodID;
    }

    public boolean isForSell() {
        return forSell;
    }

    public void setForSell(boolean forSell) {
        this.forSell = forSell;
    }

    public boolean isInTransaction() {
        return inTransaction;
    }

    public void setInTransaction(boolean inTransaction) {
        this.inTransaction = inTransaction;
    }

    private void checkArguments(int goodID, User owner) throws GoodException {
        if (goodID < 0) {
            throw new GoodException("GoodID must be a non-negative value!");
        }
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getWriteTimeStamp() {
        return writeTimeStamp;
    }

    public void setWriteTimeStamp(int writeTimeStamp) {
        this.writeTimeStamp = writeTimeStamp;
    }

    public ArrayList<Pair<Integer, Boolean>> getReadList() {
        return readList;
    }

    public void setReadList(ArrayList<Pair<Integer, Boolean>> readList) {
        this.readList = readList;
    }

    public byte[] getSigma() {
        return sigma;
    }

    public void setSigma(byte[] sigma) {
        this.sigma = sigma;
    }

    public void initialize() {
        timeStamp = 0;
        writeTimeStamp = 0;
        readList = new ArrayList<>();
        sigma = null;
    }
}
