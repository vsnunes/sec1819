package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.interfaces.NotaryByzantineInterface;

import java.util.concurrent.Callable;

public class NotaryByzantineTask implements Callable<Good> {
    public enum Operation {INTENTION2SELL, GETSTATEOFGOOD, TRANSFERGOOD}

    private NotaryByzantineInterface notaryByzantineInterface;
    private Operation operation;
    private  int ownerID;
    private int buyerID;
    private boolean state;
    private int goodID;

    /*used for transfergood operations*/
    public NotaryByzantineTask(NotaryByzantineInterface notaryByzantineInterface, Operation operation, int ownerID, int buyerID) {
        this.notaryByzantineInterface = notaryByzantineInterface;
        this.operation = operation;
        this.ownerID = ownerID;
        this.buyerID = buyerID;
    }

    /*used for intentionToSell operations*/
    public NotaryByzantineTask(NotaryByzantineInterface notaryByzantineInterface, Operation operation, boolean state, int goodID) {
        this.notaryByzantineInterface = notaryByzantineInterface;
        this.operation = operation;
        this.state = state;
        this.goodID = goodID;
    }

    /*used for getStateOfGood operations*/
    public NotaryByzantineTask(NotaryByzantineInterface notaryByzantineInterface, Operation operation, int goodID) {
        this.notaryByzantineInterface = notaryByzantineInterface;
        this.operation = operation;
        this.goodID = goodID;
    }

    @Override
    public Good call() throws Exception {
        switch (operation) {
            case INTENTION2SELL: return notaryByzantineInterface.receiveWriteIntention(state,goodID);
            case GETSTATEOFGOOD: return notaryByzantineInterface.receiveReadGetState(goodID);
            case TRANSFERGOOD:   return notaryByzantineInterface.receiveWriteTransfer(ownerID,buyerID);
        }
        return null;
    }
}
