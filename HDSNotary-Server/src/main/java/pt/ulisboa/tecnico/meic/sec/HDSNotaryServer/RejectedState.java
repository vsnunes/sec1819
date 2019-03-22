package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;

import java.rmi.RemoteException;

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
        try {
            NotaryService.getInstance().doDeleteTransaction(transaction);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (GoodException e) {
            e.printStackTrace();
        }

    }
}
