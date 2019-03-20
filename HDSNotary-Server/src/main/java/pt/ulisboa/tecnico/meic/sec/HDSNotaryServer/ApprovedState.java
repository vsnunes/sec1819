package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

public class ApprovedState extends TransactionState {
    public ApprovedState() {
        super("Approved");
    }

    @Override
    public void execute(Transaction transaction) {
        Good good = transaction.getGood();

        synchronized (this) {
            good.setOwner(transaction.getBuyer());
            //release the item from the transaction
            good.setInTransaction(false);

        }
    }
}
