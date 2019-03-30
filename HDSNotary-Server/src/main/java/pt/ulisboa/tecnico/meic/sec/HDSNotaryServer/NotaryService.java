package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.Certification;
import pt.ulisboa.tecnico.meic.sec.util.Digest;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;
import pt.ulisboa.tecnico.meic.sec.util.VirtualCertificate;

import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.Main.USERS_CERTS_FOLDER;
import static pt.ulisboa.tecnico.meic.sec.util.CertificateHelper.*;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A Class for implementing NotaryInterface on Server
 */

public class NotaryService extends UnicastRemoteObject implements NotaryInterface,Serializable {

    /** HashMap for match the ID with the object even though the object has an ID**/
    private HashMap<Integer, User> users;
    private HashMap<Integer, Good> goods;

    /** Every transaction has an ID so keeps record of the last ID used in a transaction **/
    private int transactionCounter = 0;

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
    public Interaction intentionToSell(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        int goodId = request.getGoodID();
        int userId = request.getUserID();
        boolean bool = request.getResponse();

        Certification cert = new VirtualCertificate();
        try {
            cert.init(new File("../HDSNotaryLib/src/main/resources/certs/user" + userId + ".crt").getAbsolutePath(),
                    new File("../HDSNotaryLib/src/main/resources/certs/java_certs/private_user" + userId + "_pkcs8.pem" ).getAbsolutePath());
        } catch (HDSSecurityException e) {
            e.printStackTrace();
        }

        try {
            /*compare hmacs*/
            if(Digest.verify(request, cert) == false){
                throw new HDSSecurityException("Tampering detected!");
            }
            /*check freshness*/
            if(request.getUserClock() != getClock(userId)){
                throw new HDSSecurityException("Replay attack detected!!");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RemoteException(e.getMessage());
        }

        Good good = goods.get(goodId);
        if(good != null){

            //Check if user has privileges to sell an item
            if (good.getOwner().getUserID() != userId) {
                throw new GoodException("Good doesn't belong to you!");
            }
            if(bool) {
                good.setForSell(bool);
                good.getOwner().getClock().increment();
                doWrite();
            }

            return putHMAC(request);
        }
        else{
            throw new GoodException("Good does not exist.");
        }

    }


    @Override
    public Interaction getStateOfGood(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        int goodId = request.getGoodID();


        Good good = goods.get(goodId);
        if(good != null){
            request.setResponse(good.isForSell());
            return putHMAC(request);
        }
        else{
            throw new GoodException("Good does not exist.");
        }

    }

    @Override
    public Interaction transferGood(Interaction request) throws RemoteException, TransactionException, HDSSecurityException {
        int goodId = request.getGoodID();
        int sellerId = request.getSellerID();
        int buyerId = request.getBuyerID();

        Good good = goods.get(goodId);
        User seller = users.get(sellerId);
        User buyer = users.get(buyerId);

        Transaction transaction = new Transaction(transactionCounter++, seller, buyer, good);
        doWriteTransaction(transaction);
        transaction.execute();

        if (transaction.getTransactionStateDescription().equals("Approved")) {
            transaction.execute(); //change the ownership of the good
            seller.getClock().increment();
            buyer.getClock().increment();
            doWrite();
            request.setResponse(true);
            return putHMAC(request);
        }
        else {
            throw new TransactionException(transaction.getState().getObs());
        }
    }

    @Override
    public int getClock(int userID) throws RemoteException {
        Iterator iterator = users.keySet().iterator();
        while (iterator.hasNext()) {
            Integer key = (Integer) iterator.next();
            if(users.get(key).getUserID() == userID){
                return users.get(key).getClock().getClockValue();
            }
        }
        return -1;
    }

    private Interaction putHMAC(Interaction request){
        Certification cert = new VirtualCertificate();
        try {
            cert.init(new File("../HDSNotaryLib/src/main/resources/certs/rootca.crt").getAbsolutePath(),
                    new File("../HDSNotaryLib/src/main/resources/certs/java_certs/private_rootca_pkcs8.pem" ).getAbsolutePath());
        } catch (HDSSecurityException e) {
            e.printStackTrace();
        }
        try {
            request.setHmac(Digest.createDigest(request, cert));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (HDSSecurityException e) {
            e.printStackTrace();
        }
        return request;
    }


    protected void createUser(){
        try {
            users.put(1, new User(1, readPublicKey(USERS_CERTS_FOLDER + "user1.crt")));
            users.put(2, new User(2, readPublicKey(USERS_CERTS_FOLDER + "user2.crt")));
            users.put(3, new User(3, readPublicKey(USERS_CERTS_FOLDER + "user3.crt")));
            users.put(4, new User(4, readPublicKey(USERS_CERTS_FOLDER + "user4.crt")));
            users.put(5, new User(5, readPublicKey(USERS_CERTS_FOLDER + "user5.crt")));
            System.out.println("** Creating Users: All users' certificates were loaded successfully!");
        }catch (CertificateException e) {
            System.err.println("** Creating User: Failed to load certificate!");
        } catch (IOException e){
            System.err.println("** Creating User: IO Problem!");
        }
    }

    protected void createGood() throws GoodException{
        goods.put(1,new Good(1, users.get(1)));
        goods.put(2,new Good(2, users.get(2)));
        goods.put(3,new Good(3, users.get(3)));
        goods.put(4,new Good(4, users.get(4)));
        goods.put(5,new Good(5, users.get(5)));

    }









    /*
    ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************+++++++++DEBUG/PERSISTENCE+++++++***************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************
     ********************************************************************************************************************

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
            HashMap<Integer, User> usersTest = (HashMap<Integer, User>) oi.readObject();

            Iterator iterator = test.keySet().iterator();
            System.out.println("=================================================================");
            while (iterator.hasNext()) {
                Integer key = (Integer) iterator.next();
                System.out.printf("Good: %d\tOwner: %d\tOwner clock: %d\tIs4Sale: %s\tOwner PubK OK?: %s\n", test.get(key).getGoodID(), test.get(key).getOwner().getUserID(), test.get(key).getOwner().getClock().getClockValue(), test.get(key).isForSell(), test.get(key).getOwner().getPublicKey() != null);
            }
            System.out.println("=================================================================");
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

            while(true) {
                oi = new ObjectInputStream(fi);
                transactionCounter = (int) oi.readObject();
                System.out.println("transactionCounter " + transactionCounter + " was recovered");
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

            while(true) {
                oi = new ObjectInputStream(fi);
                transactionCounter = (int) oi.readObject();
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
        System.out.println("Deleting transaction...");
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



}
