package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

/**
 * Transaction Pending State.
 * In this state the Notary checks for the correctness of both entities evolved in the transaction as well the good
 * to be sell.
 */
public class PendingState extends TransactionState {

    public PendingState() {
        super("Pending");
    }



    @Override
    public void execute(Transaction transaction) {
        Good good = transaction.getGood();

        //Item is on another transaction concurrently
        if (good.isInTransaction()) {
            transaction.setState(new CancelledState());
        }

        //Item is not for sell
        else if (good.isForSell() == false)
            transaction.setState(new RejectedState());

        //Seller is not the owner of the item!
        else if (!transaction.getSeller().equals(good.getOwner())) {
            transaction.setState(new RejectedState());
        }
        else {
            //prevent setInTransaction to be changed
            synchronized (this) {
                good.setInTransaction(true);
                transaction.setState(new ApprovedState());
            }
        }
    }
}
