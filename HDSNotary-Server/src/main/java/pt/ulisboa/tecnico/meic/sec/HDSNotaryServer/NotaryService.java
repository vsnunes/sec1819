package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A Class for implementing NotaryInterface on Server
 */

public class NotaryService extends UnicastRemoteObject implements NotaryInterface,Serializable {

    private HashMap<Integer, User> users;
    private HashMap<Integer, Good> goods;
    private Transaction transaction;

    public NotaryService() throws RemoteException, GoodException {
        super();
        if(!doRead()){
            System.out.println("No data found, initializing...");
            users = new HashMap<>();
            goods = new HashMap<>();
            createUser();
            createGood();
            doWrite();
        }
        doPrint();

    }

    @Override
    public boolean intentionToSell(int userId, int goodId, boolean bool) throws RemoteException, GoodException {
        Good good = goods.get(goodId);
        if(good != null){

            //Check if user has privileges to sell an item
            if (good.getOwnerID() != userId) {
                throw new GoodException("Good doesn't belong to you!");
            }

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
    public boolean transferGood(int sellerId, int buyerId, int goodId) throws RemoteException, TransactionException {
        Good good = goods.get(goodId);
        User seller = users.get(sellerId);
        User buyer = users.get(buyerId);

        transaction = new Transaction(1, seller, buyer, good);
        transaction.execute();

        if (transaction.getTransactionStateDescription().equals("Approved")) {
            transaction.execute(); //change the ownership of the good
            doWrite();
            return true;
        }
        else {
            throw new TransactionException(transaction.getState().getObs());
        }
    }

    /*
    To be called when state changes
     */
    private void doWrite(){
        System.out.println("Writing...");
        try {
            File file = new File("myObjects.bin");
            file.createNewFile();
            FileOutputStream f = new FileOutputStream(file, false);
            ObjectOutputStream o = new ObjectOutputStream(f);


            o.writeObject(goods);
            System.out.println("The Object goods was succesfully written to a file");
            o.writeObject(users);
            System.out.println("The Object users was succesfully written to a file");


            o.close();
        }
         catch (IOException e) {
            System.out.println("Error initializing stream");
        }
    }

    /*
    To be called when notary service starts
     */
    private boolean doRead() {
        try {
            File file = new File("myObjects.bin");
            if(!file.exists()){
                System.out.println("File does not exists");
                return false;
            }
            //path to be defined
            FileInputStream fi = new FileInputStream(file);
            ObjectInputStream oi = new ObjectInputStream(fi);

            // Read objects

            goods = (HashMap<Integer, Good>) oi.readObject();
            System.out.println("The Object goods has been read from the file...");
            users = (HashMap<Integer, User>) oi.readObject();
            System.out.println("The Object users has been read from the file...");

            oi.close();

            return true;
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
            e.printStackTrace();
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
        goods.put(1,new Good(1, users.get(1).getUserID()));
        goods.put(2,new Good(2, users.get(2).getUserID()));
        goods.put(3,new Good(3, users.get(3).getUserID()));
        goods.put(4,new Good(4, users.get(4).getUserID()));
        goods.put(5,new Good(5, users.get(5).getUserID()));

    }
    public void doPrint(){
        try {
            //path to be defined
            FileInputStream fi = new FileInputStream(new File("myObjects.bin"));
            ObjectInputStream oi = new ObjectInputStream(fi);

            // Read objects

            HashMap<Integer, Good> test = (HashMap<Integer, Good>) oi.readObject();
            System.out.println("The Object goods has been read from the file...");
            System.out.println(test);

            Iterator iterator = test.keySet().iterator();
            while (iterator.hasNext()) {
                Integer key = (Integer) iterator.next();
                System.out.println("Owner: " + test.get(key).getOwnerID() + " Good: " + test.get(key).getGoodID());
            }
            oi.close();

        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
            e.printStackTrace();
        }
    }

}
