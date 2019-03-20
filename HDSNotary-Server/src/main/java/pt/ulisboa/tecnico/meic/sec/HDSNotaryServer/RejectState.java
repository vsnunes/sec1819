package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

public class RejectState extends TransactionState {

    public RejectState() {
        super("Reject");
    }

    @Override
    public void execute(Transaction transaction) {

    }
}
