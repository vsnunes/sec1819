package pt.ulisboa.tecnico.meic.sec.interfaces;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotaryInterface extends Remote {

    /**
     * Request to the Notary a change of an intention to Sell a Good
     * @param request of the owner of the good
     * @return response containing the result of the operation properly secure (HMAC, clocks, etc...)
     * @throws RemoteException
     * @throws GoodException
     * @throws HDSSecurityException
     */
    Interaction intentionToSell(Interaction request) throws RemoteException, GoodException, HDSSecurityException;

    /**
     * Request to Notary the State of Given Good is for sell or not
     * @param request of the owner of the good
     * @return response containing the result of the operation properly secure (HMAC, clocks, etc...)
     * @throws RemoteException
     * @throws GoodException
     * @throws HDSSecurityException
     */
    Interaction getStateOfGood(Interaction request) throws RemoteException, GoodException, HDSSecurityException;

    /**
     * Request to Notary certify and approve the change of ownership of a good.
     * @param request of the owner of the good
     * @return response containing the result of the operation properly secure (HMAC, clocks, etc...)
     * @throws RemoteException
     * @throws TransactionException
     * @throws GoodException
     * @throws HDSSecurityException
     */
    Interaction transferGood(Interaction request) throws RemoteException, TransactionException, GoodException, HDSSecurityException;

    /**
     * Returns current clock user
     * @param userID whose clock is requested
     * @return integer value representing the current clock of the user
     * @throws RemoteException
     */
    int getClock(int userID) throws RemoteException;

    /* debug only */
    void doPrint() throws RemoteException;
}
