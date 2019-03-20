package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.io.*;
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
        if(!doRead()){
            users = new HashMap<>();
            goods = new HashMap<>();
        }

    }

    @Override
    public boolean intentionToSell(int userId, int goodId, boolean bool) throws RemoteException, GoodException {
        Good good = goods.get(goodId);
        if(good != null){
            good.setForSell(bool);
            doWrite();
            return good.isForSell();
        }
        else{
            throw new GoodException("Good does not exist.");
        }

    }

    @Override
    public boolean getStateOfGood(int goodId) throws RemoteException, GoodException {
        Good good = goods.get(goodId);
        if(good != null){
            return good.isForSell();
        }
        else{
            throw new GoodException("Good does not exist.");
        }

    }

    @Override
    public boolean transferGood(int sellerId, int buyerId, int goodId) throws RemoteException {
        Good good = goods.get(goodId);
        User seller = users.get(sellerId);
        User buyer = users.get(buyerId);

        Transaction transaction = new Transaction(1, seller, buyer, good);
        transaction.execute();

        if (transaction.getTransactionStateDescription().equals("Approved")) {
            doWrite();
            return true;
        }
        return false;
    }

    /*
    To be called when state changes
     */
    private void doWrite(){
        try {
            FileOutputStream f = new FileOutputStream(new File("myObjects.bin"));
            ObjectOutputStream o = new ObjectOutputStream(f);

            o.writeObject(users);
            o.writeObject(goods);

            o.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        }
    }

    /*
    To be called when notary service starts
     */
    private boolean doRead() {
        try {
            //path to be defined
            FileInputStream fi = new FileInputStream(new File("myObjects.bin"));
            ObjectInputStream oi = new ObjectInputStream(fi);

            // Read objects
            HashMap<Integer, User> users = (HashMap<Integer, User>) oi.readObject();
            HashMap<Integer, Good> goods = (HashMap<Integer, Good>) oi.readObject();

            oi.close();
            fi.close();

            return true;
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
            e.printStackTrace();
        }
        return false;
    }

    protected void createUser(){
        users.put(1, new User(1,1));
        users.put(2, new User(2,2));
        users.put(3, new User(3,3));
        users.put(4, new User(4,4));
        users.put(5, new User(5,5));
    }

    protected void createGood() throws GoodException{
        goods.put(1,new Good(1, users.get(1)));
        goods.put(2,new Good(2, users.get(2)));
        goods.put(3,new Good(3, users.get(3)));
        goods.put(4,new Good(4, users.get(4)));
        goods.put(5,new Good(5, users.get(5)));

    }

}
