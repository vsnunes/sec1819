package pt.ulisboa.tecnico.meic.sec.interfaces;

import pt.ulisboa.tecnico.meic.sec.util.Interaction;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {

    Interaction buyGood(Interaction request) throws RemoteException;

}
