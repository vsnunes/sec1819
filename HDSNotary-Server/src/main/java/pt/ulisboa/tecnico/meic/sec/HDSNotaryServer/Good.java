package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;

import java.io.Serializable;

/**
 * A class for describing Good items
 */
public class Good implements Serializable {

    /** The good identifier **/
    private int goodID;

    private boolean forSell;

    private int ownerID;

    /** Prevents the good from concurrent transactions **/
    private boolean inTransaction;

    Good(int goodID, int owner) throws GoodException {
        checkArguments(goodID, owner);
        this.goodID = goodID;
        this.forSell = false;
        this.ownerID = owner;
        this.inTransaction = false;
    }

    Good(int goodID, int owner, boolean forSell) throws GoodException {
        checkArguments(goodID, owner);
        this.goodID = goodID;
        this.forSell = forSell;
        this.ownerID = owner;
        this.inTransaction = false;
    }

    public void setGoodID(int goodID) throws GoodException{
        checkArguments(goodID, this.getOwnerID());
        this.goodID = goodID;
    }

    public int getOwnerID() {
        return this.ownerID;
    }

    public void setOwnerID(int owner) {
        this.ownerID = owner;
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

    private void checkArguments(int goodID, int owner) throws GoodException {
        if (goodID < 0) {
            throw new GoodException("GoodID must be a non-negative value!");
        }
    }
}
