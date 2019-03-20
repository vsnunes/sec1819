package pt.ulisboa.tecnico.meic.sec.interfaces;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotaryInterface extends Remote {

    boolean intentionToSell(int userId, int goodId, boolean bool) throws RemoteException, GoodException;

    boolean getStateOfGood(int goodId) throws RemoteException, GoodException;

    boolean transferGood(int sellerId, int buyerId, int goodId) throws RemoteException, TransactionException;
}
