package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import javafx.util.Pair;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * A Class for implementing NotaryInterface on Server
 */

public class NotaryService extends UnicastRemoteObject implements NotaryInterface {

    HashMap<Integer, User> users;
    HashMap<Integer, Good> goods;
    HashMap<Good, User> ownerMap;

    public NotaryService() throws RemoteException {
        super();
        users = new HashMap<Integer, User>();
        goods = new HashMap<Integer, Good>();
        ownerMap = new HashMap<Good, User>();
    }

    @Override
    public boolean intentionToSell(int userId, int goodId, boolean bool) throws RemoteException {
        Good good = goods.get(goodId);
        good.setForSell(bool);

        return good.isForSell();
    }

    @Override
    public Pair<Integer, Boolean> getStateOfGood(int goodId) throws RemoteException {
        return null;
    }

    @Override
    public boolean transferGood(int sellerId, int buyerId, int goodId) throws RemoteException {
        return false;
    }

}
