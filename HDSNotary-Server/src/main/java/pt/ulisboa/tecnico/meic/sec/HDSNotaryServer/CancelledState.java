package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

public class CancelledState extends TransactionState {

    public CancelledState() {
        super("Cancelled");
    }

    @Override
    public void execute(Transaction transaction) {

    }
}
