package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;

import java.rmi.RemoteException;

/**
 * Transaction Approved State.
 * The notary already check the transaction everything was OK. Perform the ownership changing of the item.
 */
public class ApprovedState extends TransactionState {
    public ApprovedState() {
        super("Approved");
    }

    @Override
    public void execute(Transaction transaction) {
        Good good = transaction.getGood();

        synchronized (this) {
            //write(good.getOwner().getUserID(), transaction.getBuyer().getUserID())
            good.setOwner(transaction.getBuyer());

            //item is now NOT for sell
            good.setForSell(false);

            //release the item from the transaction
            good.setInTransaction(false);
            try {
                NotaryService.getInstance().doDeleteTransaction(transaction);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (GoodException e) {
                e.printStackTrace();
            }

        }
    }
}
