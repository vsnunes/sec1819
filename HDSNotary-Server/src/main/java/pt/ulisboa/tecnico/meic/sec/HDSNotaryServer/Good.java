package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

/**
 * A class for describing Good items
 */
public class Good {

    /** The good identifier **/
    private int goodID;

    private boolean forSell;

    Good() {
        this.goodID = 0;
        this.forSell = false;
    }

    Good(int goodID) throws GoodException {
        checkArguments(goodID);
        this.goodID = goodID;
        this.forSell = false;
    }

    Good(int goodID, boolean forSell) throws GoodException {
        checkArguments(goodID);
        this.goodID = goodID;
        this.forSell = forSell;
    }

    public void setGoodID(int goodID) throws GoodException{
        checkArguments(goodID);
        this.goodID = goodID;
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

    private void checkArguments(int goodID) throws GoodException {
        if (goodID < 0) {
            throw new GoodException("GoodID must be a non-negative value!");
        }
    }
}
