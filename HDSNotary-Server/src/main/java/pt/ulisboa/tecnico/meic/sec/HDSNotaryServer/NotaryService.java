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

        if(good != null && seller != null && buyer != null){
            if(good.isForSell()){
                if(good.getOwner() == seller){
                        good.setOwner(buyer);
                        doWrite();
                        return true;
                }
            }
        }
        doWrite();
        return false;
    }

    /*
    To be called when state changes
     */
    public void doWrite(){
        try {
            FileOutputStream f = new FileOutputStream(new File("myObjects.txt"));
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
    public boolean doRead() {
        try {
            //path to be defined
            FileInputStream fi = new FileInputStream(new File("somefile.txt"));
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

}
