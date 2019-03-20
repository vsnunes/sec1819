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

            TransactionState state = new CancelledState();
            state.setObs("Good is on another transaction concurrently");

            transaction.setState(state);
        }

        //Item is not for sell
        else if (good.isForSell() == false) {
            TransactionState state = new RejectedState();
            state.setObs("Good is not for sale!");

            transaction.setState(state);
        }

        //Seller is not the owner of the item!
        else if (!transaction.getSeller().equals(good.getOwner())) {

            TransactionState state = new RejectedState();
            state.setObs("Good IS NOT owned by you therefore you cannot sell it!");

            transaction.setState(state);
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
