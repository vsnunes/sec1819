package pt.ulisboa.tecnico.meic.sec.interfaces;

import Exceptions.GoodException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotaryInterface extends Remote {

    boolean intentionToSell(int userId, int goodId, boolean bool) throws RemoteException, GoodException;

    boolean getStateOfGood(int goodId) throws RemoteException, GoodException;

    boolean transferGood(int sellerId, int buyerId, int goodId) throws RemoteException;
}
