package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * A Class for implementing NotaryInterface on Server
 */

public class NotaryService extends UnicastRemoteObject implements NotaryInterface {

    private HashMap<Integer, User> users;
    private HashMap<Integer, Good> goods;

    public NotaryService() throws RemoteException {
        super();
        users = new HashMap<>();
        goods = new HashMap<>();
    }

    @Override
    public boolean intentionToSell(int userId, int goodId, boolean bool) throws RemoteException {
        Good good = goods.get(goodId);
        good.setForSell(bool);

        return good.isForSell();
    }

    @Override
    public boolean getStateOfGood(int goodId) throws RemoteException {
        Good good = goods.get(goodId);

        return good.isForSell();
    }

    @Override
    public boolean transferGood(int sellerId, int buyerId, int goodId) throws RemoteException {
        Good good = goods.get(goodId);
        User seller = users.get(sellerId);
        User buyer = users.get(buyerId);

        if(good != null && seller != null && buyer != null){
            if(good.isForSell()){
                if(good.getOwner() == seller){
                        good.setOwner(buyer);
                        return true;
                }
            }
        }
        return false;
    }

}
