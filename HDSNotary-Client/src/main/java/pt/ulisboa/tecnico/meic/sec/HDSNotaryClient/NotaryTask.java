package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;

import java.util.concurrent.Callable;

/**
 * Class describing Notary Task for async calls
 */
public class NotaryTask implements Callable<Interaction> {

    public enum Operation {INTENTION2SELL, GETSTATEOFGOOD, TRANSFERGOOD}

    private NotaryInterface notaryInterface;
    private Operation operation;
    private Interaction params;

    public NotaryTask(NotaryInterface notaryInterface, Operation operation, Interaction parms) {
        this.notaryInterface = notaryInterface;
        this.operation = operation;
        this.params = parms;
    }

    @Override
    public Interaction call() throws Exception {
        Thread.sleep(10000);
        switch (operation) {
            case INTENTION2SELL: return notaryInterface.intentionToSell(params);
            case GETSTATEOFGOOD: return notaryInterface.getStateOfGood(params);
            case TRANSFERGOOD:   return notaryInterface.transferGood(params);
        }
        return null;
    }
}
