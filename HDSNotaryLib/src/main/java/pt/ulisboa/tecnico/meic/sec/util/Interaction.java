package pt.ulisboa.tecnico.meic.sec.util;

public class Interaction {
    private byte[] hmac;
    private int buyerID;
    private int sellerID;
    private boolean response;
    private int goodID;
    private int userID;

    public Interaction(){
        hmac = null;
        buyerID = 0;
        sellerID = 0;
        response = false;
        goodID = 0;
        userID = 0;
    }

    public byte[] getHmac() {
        return hmac;
    }

    public void setHmac(byte[] hmac) {
        this.hmac = hmac;
    }

    public int getBuyerID() {
        return buyerID;
    }

    public void setBuyerID(int buyerID) {
        this.buyerID = buyerID;
    }

    public int getSellerID() {
        return sellerID;
    }

    public void setSellerID(int sellerID) {
        this.sellerID = sellerID;
    }

    public boolean isResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }

    public int getGoodID() {
        return goodID;
    }

    public void setGoodID(int goodID) {
        this.goodID = goodID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "" + buyerID + sellerID + response + goodID + userID;
    }
}
