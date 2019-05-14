package pt.ulisboa.tecnico.meic.sec.util;

import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A Class for describing interaction between entities (Notary <-> User) or (User <-> User)
 * This class will encapsulate all request arguments OR all response arguments.
 */
public class Interaction implements Serializable {

    public enum Type {TRANSFERGOOD, INTENTION2SELL}

    private Type type;

    private byte[] hmac;
    private byte[] sellerHMAC;
    private byte[] buyerHMAC;
    private int buyerID;
    private int sellerID;
    private boolean response;
    private int goodID;
    private int userID;
    private int userClock;
    private int buyerClock;
    private int sellerClock;

    /** used for atomic and regular register */
    private int wts;
    private byte[] sigma;
    private int ownerID;
    private int ownerClock;
    private byte[] lastChangeHMAC;
    private byte[] lastChangeHMACSeller;

    /** used for reliable broadcast */
    private int notaryID;
    /** it is used for echo message passing */
    private byte[] notaryIDSignature;
    private byte[] readySignature;
    private int echoClock;
    private int readyClock;

    public Interaction() {
        hmac = null;
        sellerHMAC = null;
        buyerHMAC = null;
        buyerID = 0;
        sellerID = 0;
        response = false;
        goodID = 0;
        userID = 0;
        userClock = 0;
        buyerClock = 0;
        sellerClock = 0;
        wts = 0;
        lastChangeHMAC = null;
        lastChangeHMACSeller = null;
        type = null;
    }

    public byte[] getHmac() {
        return hmac;
    }

    public void setHmac(byte[] hmac) {
        this.hmac = hmac;
    }

    public byte[] getSellerHMAC() {
        return sellerHMAC;
    }

    public void setSellerHMAC(byte[] sellerHMAC) {
        this.sellerHMAC = sellerHMAC;
    }

    public byte[] getBuyerHMAC() {
        return buyerHMAC;
    }

    public void setBuyerHMAC(byte[] buyerHMAC) {
        this.buyerHMAC = buyerHMAC;
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

    public boolean getResponse() {
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

    public boolean isResponse() {
        return response;
    }

    public int getUserClock() {
        return userClock;
    }

    public void setUserClock(int userClock) {
        this.userClock = userClock;
    }

    public int getBuyerClock() {
        return buyerClock;
    }

    public void setBuyerClock(int buyerClock) {
        this.buyerClock = buyerClock;
    }

    public int getSellerClock() {
        return sellerClock;
    }

    public void setSellerClock(int sellerClock) {
        this.sellerClock = sellerClock;
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

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public int getOwnerClock() {
        return ownerClock;
    }

    public void setOwnerClock(int ownerClock) {
        this.ownerClock = ownerClock;
    }

    public byte[] getLastChangeHMAC() {
        return lastChangeHMAC;
    }

    public void setLastChangeHMAC(byte[] lastChangeHMAC) {
        this.lastChangeHMAC = lastChangeHMAC;
    }

    public byte[] getLastChangeHMACSeller() {
        return this.lastChangeHMACSeller;
    }

    public void setLastChangeHMACSeller(byte[] lastChangeHMACSeller) {
        this.lastChangeHMACSeller = lastChangeHMACSeller;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public byte[] getNotaryIDSignature() {
        return notaryIDSignature;
    }

    public void setNotaryIDSignature(byte[] notaryIDSignature) {
        this.notaryIDSignature = notaryIDSignature;
    }

    public int getNotaryID() {
        return notaryID;
    }

    public void setNotaryID(int notaryID) {
        this.notaryID = notaryID;
    }

    public int getReadyClock() {
        return readyClock;
    }

    public void setReadyClock(int readyClock) {
        this.readyClock = readyClock;
    }

    public int getEchoClock() {
        return echoClock;
    }

    public void setEchoClock(int echoClock) {
        this.echoClock = echoClock;
    }

    public byte[] getReadySignature() {
        return readySignature;
    }

    public void setReadySignature(byte[] readySignature) {
        this.readySignature = readySignature;
    }

    public String echoString() {
        return "Interaction{" +
                "type=" + type +
                ", hmac=" + Arrays.toString(hmac) +
                ", sellerHMAC=" + Arrays.toString(sellerHMAC) +
                ", buyerHMAC=" + Arrays.toString(buyerHMAC) +
                ", buyerID=" + buyerID +
                ", sellerID=" + sellerID +
                ", response=" + response +
                ", goodID=" + goodID +
                ", userID=" + userID +
                ", userClock=" + userClock +
                ", buyerClock=" + buyerClock +
                ", sellerClock=" + sellerClock +
                ", wts=" + wts +
                ", sigma=" + Arrays.toString(sigma) +
                ", ownerID=" + ownerID +
                ", ownerClock=" + ownerClock +
                ", lastChangeHMAC=" + Arrays.toString(lastChangeHMAC) +
                ", lastChangeHMACSeller=" + Arrays.toString(lastChangeHMACSeller) +
                ", notaryID=" + notaryID +
                ", echoClock=" + echoClock +
                '}';
    }

    public String readyString() {
        return "Interaction{" +
                "type=" + type +
                ", hmac=" + Arrays.toString(hmac) +
                ", sellerHMAC=" + Arrays.toString(sellerHMAC) +
                ", buyerHMAC=" + Arrays.toString(buyerHMAC) +
                ", buyerID=" + buyerID +
                ", sellerID=" + sellerID +
                ", response=" + response +
                ", goodID=" + goodID +
                ", userID=" + userID +
                ", userClock=" + userClock +
                ", buyerClock=" + buyerClock +
                ", sellerClock=" + sellerClock +
                ", wts=" + wts +
                ", sigma=" + Arrays.toString(sigma) +
                ", ownerID=" + ownerID +
                ", ownerClock=" + ownerClock +
                //", lastChangeHMAC=" + Arrays.toString(lastChangeHMAC) +
                //", lastChangeHMACSeller=" + Arrays.toString(lastChangeHMACSeller) +
                ", notaryID=" + notaryID +
                ", echoClock=" + echoClock +
                ", readyClock=" + readyClock +
                ", notaryIDSignature" + Arrays.toString(notaryIDSignature) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Interaction)) return false;
        Interaction that = (Interaction) o;
        return getBuyerID() == that.getBuyerID() &&
                getSellerID() == that.getSellerID() &&
                isResponse() == that.isResponse() &&
                getGoodID() == that.getGoodID() &&
                getUserID() == that.getUserID() &&
                getUserClock() == that.getUserClock() &&
                getBuyerClock() == that.getBuyerClock() &&
                getSellerClock() == that.getSellerClock() &&
                getWts() == that.getWts() &&
                getOwnerID() == that.getOwnerID() &&
                getOwnerClock() == that.getOwnerClock() &&
                getType() == that.getType() &&
                Arrays.equals(getHmac(), that.getHmac()) &&
                Arrays.equals(getSellerHMAC(), that.getSellerHMAC()) &&
                Arrays.equals(getBuyerHMAC(), that.getBuyerHMAC()) &&
                Arrays.equals(getSigma(), that.getSigma()) &&
                Arrays.equals(getLastChangeHMAC(), that.getLastChangeHMAC()) &&
                Arrays.equals(getLastChangeHMACSeller(), that.getLastChangeHMACSeller());
    }

    @Override
    public String toString() {
        return "" + buyerID + sellerID + response + goodID + userID + userClock + buyerClock + sellerClock;
    }


}
