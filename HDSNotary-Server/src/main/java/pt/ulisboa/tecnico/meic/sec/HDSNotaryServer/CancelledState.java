package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

/**
 * Transaction Cancelled State.
 * The notary realizes that the good is already in a pending transaction.
 */
public class CancelledState extends TransactionState {

    public CancelledState() {
        super("Cancelled");
    }

    @Override
    public void execute(Transaction transaction) {

    }
}
