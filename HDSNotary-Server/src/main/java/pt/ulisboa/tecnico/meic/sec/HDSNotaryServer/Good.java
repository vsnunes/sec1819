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

    private int wts;

    private byte[] sigma;

    private byte[] lastChangeHMAC;



    /** Prevents the good from having concurrent transactions **/
    private boolean inTransaction;

    Good(int goodID, User owner) throws GoodException {
        checkArguments(goodID, owner);
        this.goodID = goodID;
        this.forSell = false;
        this.owner = owner;
        this.inTransaction = false;
    }

    Good(int goodID, User owner, boolean forSell) throws GoodException {
        checkArguments(goodID, owner);
        this.goodID = goodID;
        this.forSell = forSell;
        this.owner = owner;
        this.inTransaction = false;
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

    public int getWts() {
        return wts;
    }

    public void setWts(int wts) {
        this.wts = wts;
    }

    public byte[] getSigma() {
        return sigma;
    }

    public void setSigma(byte[] sigma) {
        this.sigma = sigma;
    }

    public byte[] getLastChangeHMAC() {
        return lastChangeHMAC;
    }

    public void setLastChangeHMAC(byte[] lastChangeHMAC) {
        this.lastChangeHMAC = lastChangeHMAC;
    }

    private void checkArguments(int goodID, User owner) throws GoodException {
        if (goodID < 0) {
            throw new GoodException("GoodID must be a non-negative value!");
        }
    }
}
