package pt.ulisboa.tecnico.meic.sec.interfaces;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotaryInterface extends Remote {

    Interaction intentionToSell(Interaction request) throws RemoteException, GoodException, HDSSecurityException;

    Interaction getStateOfGood(Interaction request) throws RemoteException, GoodException, HDSSecurityException;

    Interaction transferGood(Interaction request) throws RemoteException, TransactionException, GoodException, HDSSecurityException;

    int getClock(int userID) throws RemoteException;

    /* debug only */
    void doPrint() throws RemoteException;
}
