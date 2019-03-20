package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

/**
 * A class for describing the TransactionState
 */
public abstract class TransactionState {

    private String stateName;

    public abstract void execute(Transaction transaction);

    public TransactionState(String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {return this.stateName;}

}
