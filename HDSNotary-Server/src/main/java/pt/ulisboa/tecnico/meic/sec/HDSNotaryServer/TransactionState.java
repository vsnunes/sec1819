package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

/**
 * A class for describing the TransactionState
 */
public abstract class TransactionState {

    private String stateName;

    /** Observation of the state **/
    private String obs;

    public abstract void execute(Transaction transaction);

    public TransactionState(String stateName) {
        this.stateName = stateName;
        this.obs = "";
    }

    public String getStateName() {return this.stateName;}

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }
}
