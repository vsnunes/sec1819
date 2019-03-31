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

    private User owner;

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

    private void checkArguments(int goodID, User owner) throws GoodException {
        if (goodID < 0) {
            throw new GoodException("GoodID must be a non-negative value!");
        }
    }
}
