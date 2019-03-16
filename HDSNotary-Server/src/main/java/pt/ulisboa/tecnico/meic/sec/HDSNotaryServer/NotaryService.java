package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * A Class for implementing NotaryInterface on Server
 */

public class NotaryService extends UnicastRemoteObject implements NotaryInterface {

    public NotaryService() throws RemoteException {
        super();
    }

    @Override
    public boolean intentionToSell(int userId, int goodId) throws RemoteException {
        return false;
    }

}
