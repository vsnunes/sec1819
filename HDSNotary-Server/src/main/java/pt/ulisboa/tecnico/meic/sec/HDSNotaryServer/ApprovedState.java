package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

public class ApprovedState extends TransactionState {
    public ApprovedState() {
        super("Approved");
    }

    @Override
    public void execute(Transaction transaction) {
        Good good = transaction.getGood();

        synchronized (this) {
            try {
                good.setOwner(transaction.getBuyer());
                //release the item from the transaction
            } catch (GoodException e) {
                transaction.setState(new CancelledState());
            }
            finally {
                //either the owner changes or the transaction is cancelled we must release the item
                good.setInTransaction(false);
            }
        }

    }
}
