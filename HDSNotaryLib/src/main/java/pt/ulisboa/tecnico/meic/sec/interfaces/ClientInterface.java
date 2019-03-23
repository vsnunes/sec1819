package pt.ulisboa.tecnico.meic.sec.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {

    boolean buyGood(int goodId, int buyerId) throws RemoteException;

}
