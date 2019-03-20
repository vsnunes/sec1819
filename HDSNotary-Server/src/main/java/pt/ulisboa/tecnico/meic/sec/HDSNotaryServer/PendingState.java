package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

/**
 * Transaction Pending State.
 */
public class PendingState extends TransactionState {

    public PendingState() {
        super("Pending");
    }



    @Override
    public void execute(Transaction transaction) {
        Good good = transaction.getGood();

        if (good.isInTransaction()) {
            transaction.setState(new CancelledState());
        }
        //Item is not for sell
        else if (good.isForSell() == false)
            transaction.setState(new RejectState());

        //Seller is not the owner of the item!
        else if (!transaction.getSeller().equals(good.getOwner())) {
            transaction.setState(new RejectState());
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
