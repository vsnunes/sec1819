package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A Class for implementing NotaryInterface on Server
 */

public class NotaryService extends UnicastRemoteObject implements NotaryInterface,Serializable {
    /*UserGood.bin*/
    private HashMap<Integer, User> users;
    private HashMap<Integer, Good> goods;
    /**/
    /*Transaction.bin*/
    private int transactionCounter = 0;
    /**/
    private static NotaryService instance;

    private NotaryService() throws RemoteException, GoodException {
        super();
        if (!doRead()) {
            System.out.println("No data found, initializing...");
            users = new HashMap<>();
            goods = new HashMap<>();
            createUser();
            createGood();
            doWrite();
        }
        else {

            try {
                doRecoverTransactions();
            } catch (TransactionException e) {
                e.getMessage();
            }
        }
        instance = this;
    }

    public static NotaryService getInstance() throws RemoteException, GoodException {
        if(instance == null){
            return new NotaryService();
        }
        return instance;
    }

    @Override
    public boolean intentionToSell(int userId, int goodId, boolean bool) throws RemoteException, GoodException {
        Good good = goods.get(goodId);
        if(good != null){

            //Check if user has privileges to sell an item
            if (good.getOwner().getUserID() != userId) {
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

        Transaction transaction = new Transaction(transactionCounter++, seller, buyer, good);
        doWriteTransaction(transaction);
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
        System.out.println("Writing GoodsUser...");
        try {
            File file = new File("UsersGoods.bin");
            file.createNewFile();
            FileOutputStream f = new FileOutputStream(file, false);
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(goods);
            //System.out.println("The Object goods was succesfully written to a file");
            o.writeObject(users);
            //System.out.println("The Object users was succesfully written to a file");
            Iterator iterator = goods.keySet().iterator();
            while (iterator.hasNext()) {
                Integer key = (Integer) iterator.next();
                o.writeObject(goods.get(key).getOwner());
                //System.out.println("The Object user with id " + goods.get(key).getOwner().getUserID() + " was succesfully written to a file");
            }

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
        System.out.println("Reading GoodsUser...");
        try {
            File file = new File("UsersGoods.bin");
            if(!file.exists()){
                System.out.println("File UsersGoods.bin does not exists");
                return false;
            }
            FileInputStream fi = new FileInputStream(file);
            ObjectInputStream oi = new ObjectInputStream(fi);
            goods = (HashMap<Integer, Good>) oi.readObject();
            //System.out.println("The Object goods has been read from the file...");
            users = (HashMap<Integer, User>) oi.readObject();
            //System.out.println("The Object users has been read from the file...");
            Iterator iterator = goods.keySet().iterator();
            while (iterator.hasNext()) {
                Integer key = (Integer) iterator.next();
                goods.get(key).setOwner(goods.get(key).getOwner());
                //System.out.println("The Object user with id " + goods.get(key).getOwner().getUserID() + " was succesfully read from file");
            }

            oi.close();
            return true;
        }
        catch (FileNotFoundException e) {
            System.out.println("File UsersGoods.bin not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
            e.printStackTrace();
        }
        return false;
    }

    public void doPrint(){
        try {
            //path to be defined

            File file = new File("UsersGoods.bin");
            if(!file.exists()){
                System.out.println("File UsersGoods.bin does not exists");
                return;
            }
            FileInputStream fi = new FileInputStream(file);
            ObjectInputStream oi = new ObjectInputStream(fi);
            HashMap<Integer, Good> test = (HashMap<Integer, Good>) oi.readObject();
            System.out.println("The Object goods has been read from the file...");
            HashMap<Integer, User> users = (HashMap<Integer, User>) oi.readObject();
            Iterator iterator = test.keySet().iterator();
            while (iterator.hasNext()) {
                Integer key = (Integer) iterator.next();
                test.get(key).setOwner(test.get(key).getOwner());
                System.out.println("The Object user with id " + test.get(key).getOwner().getUserID() + " was succesfully read from file");
            }
            iterator = test.keySet().iterator();
            while (iterator.hasNext()) {
                Integer key = (Integer) iterator.next();
                System.out.println("Owner: " + test.get(key).getOwner().getUserID() + " Good: " + test.get(key).getGoodID());
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

        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        ObjectInputStream oi = null;
        try {
            //path to be defined
            File file = new File("Transaction.bin");
            if(!file.exists()){
                System.out.println("File Transaction.bin does not exists");
                return;
            }
            FileInputStream fi = new FileInputStream(file);
            oi = new ObjectInputStream(fi);
            transactionCounter = (int) oi.readObject();
            System.out.println("transactionCounter " + transactionCounter + " was recovered");
            while(true) {
                Transaction transaction = (Transaction) oi.readObject();
                User seller = (User) oi.readObject();
                User buyer = (User) oi.readObject();
                Good good = (Good) oi.readObject();
                User owner = (User) oi.readObject();
                good.setOwner(owner);
                TransactionState tsate = (TransactionState) oi.readObject();
                transaction.setSeller(seller);
                transaction.setBuyer(buyer);
                transaction.setGood(good);
                transaction.setState(tsate);
                transactions.add(transaction);
                System.out.println("Transaction with id " + transaction.getTransactionID() + " was recovered");
            }

        }
        catch (FileNotFoundException e) {
            System.out.println("File Transaction.bin not found");
        } catch (IOException e) {
            if(transactions.size() == 0) {
                System.out.println("Error initializing stream");
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
            e.printStackTrace();
        }

        try {
            oi.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*Execute transactions pending*/
    private ArrayList<Transaction> doReadTransactions(){
        System.out.println("Reading transaction...");
        doPrint();
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        ObjectInputStream oi = null;
        try {
            //path to be defined
            File file = new File("Transaction.bin");
            if(!file.exists()){
                System.out.println("File Transaction.bin does not exists");
                return null;
            }
            FileInputStream fi = new FileInputStream(file);
            oi = new ObjectInputStream(fi);
            transactionCounter = (int) oi.readObject();
            while(true) {
                Transaction transaction = (Transaction) oi.readObject();
                //System.out.println("The Object Transaction has been read from the file...");
                User seller = (User) oi.readObject();
                //System.out.println("The Object User(seller) has been read from the file...");
                User buyer = (User) oi.readObject();
                //System.out.println("The Object User(buyer) has been read from the file...");
                Good good = (Good) oi.readObject();
                //System.out.println("The Object Good has been read from the file...");
                User owner = (User) oi.readObject();
                //System.out.println("The Object User(owner of good) has been read from the file...");
                good.setOwner(owner);
                TransactionState tsate = (TransactionState) oi.readObject();
                //System.out.println("The Object Transaction has been read from the file...");

                transaction.setSeller(seller);
                transaction.setBuyer(buyer);
                transaction.setGood(good);
                transaction.setState(tsate);
                transactions.add(transaction);
            }

        }
        catch (FileNotFoundException e) {
            System.out.println("File Transaction.bin not found");
        } catch (IOException e) {
            if(transactions.size() == 0) {
                System.out.println("Error initializing stream");
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
            e.printStackTrace();
        }

        try {
            oi.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return transactions;

    }

    /*called when transaction starts*/
    private void doWriteTransaction(Transaction transaction){
        System.out.println("Writing Transaction...");
        try {
            File file = new File("Transaction.bin");
            file.createNewFile();
            FileOutputStream f = new FileOutputStream(file, true);
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(transactionCounter);
            o.writeObject(transaction);
            //System.out.println("The Object transaction was succesfully written to a file");
            o.writeObject(transaction.getSeller());
            o.writeObject(transaction.getBuyer());
            o.writeObject(transaction.getGood());
            o.writeObject(transaction.getGood().getOwner());
            o.writeObject(transaction.getState());
            o.close();
        }
        catch (IOException e) {
            System.out.println("Error initializing stream");
        }
    }

    protected void doDeleteTransaction(Transaction transaction){
        ArrayList<Transaction> transactions = doReadTransactions();
        Transaction test = new Transaction(25, users.get(1), users.get(2), goods.get(1));
        ArrayList<Transaction> tmp = transactions;
        if(transactions != null){
            for(Transaction t:transactions){
                if(t.getTransactionID() == transaction.getTransactionID()){
                    System.out.println("Removig transaction with id " + t.getTransactionID());
                    tmp.remove(t);
                    File file = new File("Transaction.bin");
                    if(file.delete()) {
                        System.out.println(file.getName() + " is deleted!");
                    }
                    for(Transaction elem:tmp){
                        System.out.println(elem.getTransactionID());
                        doWriteTransaction(elem);
                    }
                    break;
                }
            }
        }
    }

    private void doRecoverTransactions() throws TransactionException {
        ArrayList<Transaction> transactions = doReadTransactions();
        if(transactions != null) {
            for (Transaction transaction : transactions) {
                transaction.execute();
                if (transaction.getTransactionStateDescription().equals("Approved")) {
                    transaction.execute(); //change the ownership of the good
                    doWrite();
                }
                else {
                    throw new TransactionException(transaction.getState().getObs());
                }
            }
        }

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
