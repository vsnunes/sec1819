package pt.ulisboa.tecnico.meic.sec.interfaces;

import javafx.util.Pair;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotaryInterface extends Remote {

    boolean intentionToSell(int userId, int goodId, boolean bool) throws RemoteException;

    Pair<Integer, Boolean> getStateOfGood(int goodId) throws RemoteException;

    boolean transferGood(int sellerId, int buyerId, int goodId) throws RemoteException;
}
