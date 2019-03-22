package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;

import java.rmi.RemoteException;

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
        try {
            NotaryService.getInstance().doDeleteTransaction(transaction);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (GoodException e) {
            e.printStackTrace();
        }
    }
}
