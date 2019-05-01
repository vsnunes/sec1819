package pt.ulisboa.tecnico.meic.sec.util;

import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A Class for describing interaction between entities (Notary <-> User) or (User <-> User)
 * This class will encapsulate all request arguments OR all response arguments.
 */
public class Interaction implements Serializable {
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

    private int rid;
    private int wts;




    public Interaction(){
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

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public int getWts() {
        return wts;
    }

    public void setWts(int wts) {
        this.wts = wts;
    }

    @Override
    public String toString() {
        return "" + buyerID + sellerID + response + goodID + userID + userClock + buyerClock + sellerClock;
    }
}
