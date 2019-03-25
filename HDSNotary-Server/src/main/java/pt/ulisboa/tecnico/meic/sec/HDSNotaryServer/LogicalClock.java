package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import java.io.Serializable;

public class LogicalClock implements Serializable {
    private int userID;
    private int clockValue;

    public LogicalClock(int userID, int counter){
        this.userID = userID;
        this.clockValue = counter;
    }

    public int getUserID() {
        return userID;
    }

    public int getClockValue() {
        return clockValue;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setClockValue(int clockValue) {
        this.clockValue = clockValue;
    }

    public void increment(){
        clockValue++;
    }

    @Override
    public boolean equals(Object obj) {
        LogicalClock other = (LogicalClock) obj;
        return this.clockValue == other.clockValue;
    }

    public boolean isValid(int value) {
        return value > userID;
    }
}
