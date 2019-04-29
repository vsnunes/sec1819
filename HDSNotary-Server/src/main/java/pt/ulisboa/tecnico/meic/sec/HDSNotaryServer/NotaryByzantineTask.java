package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.interfaces.NotaryByzantineService;

import java.util.concurrent.Callable;

public class NotaryByzantineTask implements Callable<Good> {
    public enum Operation {INTENTION2SELL, GETSTATEOFGOOD, TRANSFERGOOD}

    private NotaryByzantineService notaryByzantineService;
    private Operation operation;
    private  int ownerID;
    private int buyerID;
    private boolean state;
    private int goodID;

    /*used for transfergood operations*/
    public NotaryByzantineTask(NotaryByzantineService notaryByzantineService, Operation operation, int ownerID, int buyerID) {
        this.notaryByzantineService = notaryByzantineService;
        this.operation = operation;
        this.ownerID = ownerID;
        this.buyerID = buyerID;
    }

    /*used for intentionToSell operations*/
    public NotaryByzantineTask(NotaryByzantineService notaryByzantineService, Operation operation, boolean state, int goodID) {
        this.notaryByzantineService = notaryByzantineService;
        this.operation = operation;
        this.state = state;
        this.goodID = goodID;
    }

    /*used for getStateOfGood operations*/
    public NotaryByzantineTask(NotaryByzantineService notaryByzantineService, Operation operation, int goodID) {
        this.notaryByzantineService = notaryByzantineService;
        this.operation = operation;
        this.goodID = goodID;
    }

    @Override
    public Good call() throws Exception {
        switch (operation) {
            case INTENTION2SELL: return notaryByzantineService.receiveWriteIntention(state,goodID);
            case GETSTATEOFGOOD: return notaryByzantineService.receiveReadGetState(goodID);
            case TRANSFERGOOD:   return notaryByzantineService.receiveWriteTransfer(ownerID,buyerID);
        }
        return null;
    }
}
