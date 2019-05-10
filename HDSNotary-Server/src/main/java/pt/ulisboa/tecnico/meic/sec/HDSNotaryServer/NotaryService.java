package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;
import org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.*;

import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.Main.NOTARY_SERVICE_PORT;
import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.Main.USERS_CERTS_FOLDER;
import static pt.ulisboa.tecnico.meic.sec.util.CertificateHelper.*;

import java.io.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A Class for implementing NotaryInterface on Server
 */

public class NotaryService extends UnicastRemoteObject implements NotaryInterface,Serializable {

    /** HashMap for match the ID with the object even though the object has an ID**/
    private HashMap<Integer, User> users;
    private HashMap<Integer, Good> goods;

    /** Number of clients */
    public static final int NUMBER_OF_CLIENTS = 5;

    /** Number of notaries */
    public static final int NUMBER_OF_NOTARIES = 4;

    public static final int N = 4;
    /** Maximum of Byzantine faults */
    public static final int F = 1;

    public static Integer[] echoCounter;
    public static Integer[] readyCounter;

    /** Every transaction has an ID so keeps record of the last ID used in a transaction **/
    private int transactionCounter = 0;

    private static NotaryService instance;

    /** By default Notary uses virtual certificates **/
    private boolean usingVirtualCerts = true;

    private static String USERSGOODS_FILE;
    private static String TRANSACTIONS_FILE;
    private static String USERSGOODSTMP_FILE;
    private static String TRANSACTIONSTMP_FILE;



    private NotaryService() throws RemoteException, GoodException {
        super();
        USERSGOODS_FILE = "UsersGoods" + NOTARY_SERVICE_PORT + ".bin";
        USERSGOODSTMP_FILE = "UsersGoods" + NOTARY_SERVICE_PORT + "TMP.bin";
        TRANSACTIONS_FILE = "Transaction" + NOTARY_SERVICE_PORT + ".bin";
        TRANSACTIONSTMP_FILE = "Transaction" + NOTARY_SERVICE_PORT + "TMP.bin";

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
        /** remeber to make this persistent!!!!!!!! */
        echoCounter = new Integer[NUMBER_OF_NOTARIES + 1];
        readyCounter = new Integer[NUMBER_OF_NOTARIES + 1];
        instance = this;
    }

    public boolean isUsingVirtualCerts() {
        return usingVirtualCerts;
    }

    public void setUsingVirtualCerts(boolean usingVirtualCerts) {
        this.usingVirtualCerts = usingVirtualCerts;
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
        int wts = request.getWts();
        byte[] sigma = request.getSigma();

        Certification cert = new VirtualCertificate();

        cert.init(new File(System.getProperty("project.users.cert.path") + userId + System.getProperty("project.users.cert.ext")).getAbsolutePath());


        try {
            /*compare hmacs*/
            if(!Digest.verify(request, cert)){
                throw new HDSSecurityException("Tampering detected!");
            }
            /*check freshness*/
            if(request.getUserClock() <= getClock(userId)){
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

            //write(bool, goodID)
            good.setForSell(bool);
            good.setWts(wts);
            good.setSigma(sigma);
            users.get(request.getUserID()).setClock(request.getUserClock());
            request.setResponse(bool);
            request.setOwnerID(good.getOwner().getUserID());

            good.setLastChangeHMAC(request.getHmac());
            request.setType(Interaction.Type.INTENTION2SELL);
            good.setLastOperation(Good.Type.INTENTION2SELL);
            doWrite();
            return putHMAC(request, 0);

            
        }
        else{
            throw new GoodException("Good does not exist.");
        }

    }


    @Override
    public Interaction getStateOfGood(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        int goodId = request.getGoodID();
        int userId = request.getUserID();

        Certification cert = new VirtualCertificate();

        cert.init(new File(System.getProperty("project.users.cert.path") + userId + System.getProperty("project.users.cert.ext")).getAbsolutePath());


        try {
            /*compare hmacs*/
            if(!Digest.verify(request, cert)){
                throw new HDSSecurityException("Tampering detected!");
            }
            /*check freshness*/
            if(request.getUserClock() <= getClock(userId)){
                throw new HDSSecurityException("Replay attack detected!!");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RemoteException(e.getMessage());
        }


        Good good = goods.get(goodId);
        if(good != null){
            request.setResponse(good.isForSell());
            request.setWts(good.getWts());
            request.setSigma(good.getSigma());
            request.setOwnerID(good.getOwner().getUserID());
            request.setOwnerClock(good.getOwner().getClock());
            request.setLastChangeHMAC(good.getLastChangeHMAC());
            if (good.getLastOperation() == Good.Type.INTENTION2SELL)
                request.setType(Interaction.Type.INTENTION2SELL);
            else if (good.getLastOperation() == Good.Type.TRANSFERGOOD) {
                request.setBuyerID(good.getBuyerID());
                request.setSellerID(good.getSellerID());
                request.setSellerClock(good.getSellerClock());
                request.setBuyerClock(good.getBuyerClock());
                request.setType(Interaction.Type.TRANSFERGOOD);
            }
            return putHMAC(request, -1);
        }
        else{
            throw new GoodException("Good does not exist.");
        }

    }

    @Override
    public Interaction transferGood(Interaction request) throws RemoteException, TransactionException, GoodException, HDSSecurityException {
        int goodId = request.getGoodID();
        int sellerId = request.getSellerID();
        int buyerId = request.getBuyerID();
        int wts = request.getWts();
        byte[] sigma = request.getSigma();

        Good good = goods.get(goodId);
        User seller = users.get(sellerId);
        User buyer = users.get(buyerId);

       /*verificar para o buyer*/
        Certification cert = new VirtualCertificate();

        cert.init(new File(System.getProperty("project.users.cert.path") + buyerId + System.getProperty("project.users.cert.ext")).getAbsolutePath());


        try {
            /*compare hmacs*/
            String data = "" + request.getGoodID() + request.getBuyerID() + request.getBuyerClock() + request.getSellerClock();
            if(!Digest.verify(request.getBuyerHMAC(), data,  cert)){
                throw new HDSSecurityException("NotaryService: Tampering detected in Buyer! " + data);
            }
            /*check freshness*/
            if(request.getBuyerClock() <= getClock(buyerId)){
                throw new HDSSecurityException("Replay attack detected, transaction aborted!!");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        /*verificar para o seller*/
        cert = new VirtualCertificate();

        cert.init(new File(System.getProperty("project.users.cert.path") + sellerId + System.getProperty("project.users.cert.ext")).getAbsolutePath());

        try {
            /*compare hmacs*/
            String data = "" + request.getSellerID() + request.getBuyerID() + request.getGoodID() + request.getSellerClock() + request.getBuyerClock();
            if(!Digest.verify(request.getSellerHMAC(), data, cert)){
                throw new HDSSecurityException("Tampering detected in Seller! " + data);
            }
            /*check freshness*/
            if(request.getSellerClock() <= getClock(sellerId)){
                throw new HDSSecurityException("Replay attack detected, transaction aborted!!");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Transaction transaction = new Transaction(transactionCounter++, seller, buyer, good);
        doWriteTransaction(transaction);
        transaction.execute();

        if (transaction.getTransactionStateDescription().equals("Approved")) {
            transaction.execute(); //change the ownership of the good
            seller.setClock(request.getSellerClock());
            buyer.setClock(request.getBuyerClock());
            good.setWts(wts);
            good.setSigma(sigma);
            request.setResponse(true);
            request.setOwnerID(good.getOwner().getUserID());

            good.setBuyerID(request.getBuyerID());
            good.setSellerID(request.getSellerID());
            good.setLastOperation(Good.Type.TRANSFERGOOD);
            good.setLastChangeHMAC2(request.getSellerHMAC());
            good.setLastChangeHMAC(request.getBuyerHMAC());
            good.setBuyerClock(request.getBuyerClock());
            good.setSellerClock(request.getSellerClock());
            doWrite();
            return putHMAC(request, 1);
            

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
                return users.get(key).getClock();
            }
        }
        return -1;
    }

    private Interaction putHMAC(Interaction request, int option) throws HDSSecurityException {
        //HMAC using virtual certificates
        if (usingVirtualCerts) {
            Certification cert = new VirtualCertificate();
            try {
                cert.init("", new File(System.getProperty("project.notary.private")).getAbsolutePath());
                if(option == 0) {
                    request.setLastChangeHMAC(request.getHmac());
                } else if (option == 1) {
                    request.setLastChangeHMAC(request.getBuyerHMAC());
                    request.setLastChangeHMACSeller(request.getSellerHMAC());
                }

                request.setHmac(Digest.createDigest(request, cert));
                request.setNotaryID(Main.NOTARY_ID);
                
                cert.stop();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        else { //HMAC using CCSmartCard
            CCSmartCard cert = new CCSmartCard();
            try {
                cert.init();
                request.setHmac(Digest.createDigest(request, cert));

                request.setNotaryID(Main.NOTARY_ID);
                cert.stop();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
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

            File file = new File(USERSGOODSTMP_FILE);
            file.createNewFile();
            FileOutputStream f = new FileOutputStream(file, false);
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(goods);
            //System.out.println("The Object goods was succesfully written to a file");
            o.writeObject(users);
            //System.out.println("The Object users was succesfully written to a file");

            o.close();
            swapFiles(USERSGOODS_FILE,USERSGOODSTMP_FILE);
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
            File file = new File(USERSGOODS_FILE);
            if(!file.exists()){
                System.out.println("File " + USERSGOODS_FILE + " does not exists");
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
            System.out.println("File " + USERSGOODS_FILE + " not found");
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

            File file = new File(USERSGOODS_FILE);
            if(!file.exists()){
                System.out.println("File " + USERSGOODS_FILE + " does not exists");
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
                System.out.printf("Good: %d\tOwner: %d\tOwner clock: %d\tIs4Sale: %s\tOwner PubK OK?: %s\n",
                        test.get(key).getGoodID(), test.get(key).getOwner().getUserID(),
                        test.get(key).getOwner().getClock(), test.get(key).isForSell(),
                        test.get(key).getOwner().getPublicKey() != null);
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
            File file = new File(TRANSACTIONS_FILE);
            if(!file.exists()){
                System.out.println("File " + TRANSACTIONS_FILE +" does not exists");
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
            System.out.println("File " + TRANSACTIONS_FILE +" not found");
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
            File file = new File(TRANSACTIONS_FILE);
            if(!file.exists()){
                System.out.println("File " + TRANSACTIONS_FILE + " does not exists");
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
            System.out.println("File " + TRANSACTIONS_FILE + " not found");
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

    private void swapFiles(String original, String tmp) {
        System.out.println("Performing the swap of " + tmp + " ...");
        File originalFile= new File(original);
        try {
            System.out.println(originalFile.createNewFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        File tmpFile= new File(tmp);

        System.out.println(originalFile.renameTo(new File("dummy" + tmp)));
        System.out.println(tmpFile.renameTo(new File(original)));
        File dummy = new File("dummy"+tmp);
        System.out.println(dummy.delete());

    }

    /*called when transaction starts*/
    private void doWriteTransaction(Transaction transaction){
        System.out.println("Writing Transaction...");
        try {
            File file = new File(TRANSACTIONSTMP_FILE);
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
            swapFiles(TRANSACTIONS_FILE,TRANSACTIONSTMP_FILE);
        }
        catch (IOException e) {
            System.out.println("Error initializing stream");
        }
    }

    protected void doDeleteTransaction(Transaction transaction){
        System.out.println("Deleting transaction...");
        ArrayList<Transaction> transactions = doReadTransactions();
        ArrayList<Transaction> tmp = transactions;
        if(transactions != null){
            for(Transaction t:transactions){
                if(t.getTransactionID() == transaction.getTransactionID()){
                    System.out.println("Removig transaction with id " + t.getTransactionID());
                    tmp.remove(t);
                    File file = new File(TRANSACTIONS_FILE);
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

    /** debug purposes: clear server state**/
    public void reset() {
        this.users.clear();
        this.goods.clear();
        this.transactionCounter = 0;
    }

    @Override
    public void shutdown() throws RemoteException {
        //server dont need to do any post execution operations
    }


}
