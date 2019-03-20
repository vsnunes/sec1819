package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

/**
 * Transaction Reject State.
 * The notary rejects the transaction because there are issues with the seller, the buyer or with the good itself.
 */
public class RejectedState extends TransactionState {

    public RejectedState() {
        super("Rejected");
    }

    @Override
    public void execute(Transaction transaction) {

    }
}
