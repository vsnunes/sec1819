package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

/**
 * A class for describing Good items
 */
public class Good {

    /** The good identifier **/
    private int goodID;

    private boolean forSell;

    private User owner;

    Good(int goodID, User owner) throws GoodException {
        checkArguments(goodID, owner);
        this.goodID = goodID;
        this.forSell = false;
        this.owner = owner;
    }

    Good(int goodID, User owner, boolean forSell) throws GoodException {
        checkArguments(goodID, owner);
        this.goodID = goodID;
        this.forSell = forSell;
        this.owner = owner;
    }

    public void setGoodID(int goodID) throws GoodException{
        checkArguments(goodID, this.getOwner());
        this.goodID = goodID;
    }

    public User getOwner() {
        return this.owner;
    }

    public void setOwner(User owner) throws GoodException {
        checkArguments(this.getGoodID(), owner);
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

    private void checkArguments(int goodID, User owner) throws GoodException {
        if (goodID < 0) {
            throw new GoodException("GoodID must be a non-negative value!");
        }
        if (owner == null)
            throw new GoodException("Good Owner cannot be null!");
    }
}
