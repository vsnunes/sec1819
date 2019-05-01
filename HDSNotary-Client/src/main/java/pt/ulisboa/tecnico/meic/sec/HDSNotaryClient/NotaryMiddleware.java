package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import javafx.util.Pair;
import pt.ulisboa.tecnico.meic.sec.HDSNotaryClient.exceptions.NotaryMiddlewareException;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.CFGHelper;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Middleware for Notary operations
 * This class has the purpose of reuse the code that was written until this moment.
 * VERY IMPORTANT: For the client there are only one object corresponding to ONE server.
 *
 * This middleware makes the replication to N servers transparent to the client.
 */
public class NotaryMiddleware implements NotaryInterface {

    /**
     * The maximum number of replicas.
     * If the file contain more urls than this value then they will ignored!
     * 0 value means to considered all replicas from the Servers.cfg file
     */
    private static final int REPLICAS_N = 4;

    /** Maximum number of Byzantine faults
     * the maximum number of faults that may occur while preserving the correctness of the HDS Notary system
     **/
    private static final int BYZANTINE_F = 0;

    private int byzantine_quorum = ((REPLICAS_N+BYZANTINE_F)/2) + 1;

    /** The list of remote objects of the servers **/
    private List<NotaryInterface> servers;

    /** Thread pool for servers tasks **/
    private ThreadPoolExecutor poolExecutor;

    /** read id */
    private int rid = 0;

    /** write timeStamp */
    private int wts = 0;


    public NotaryMiddleware(String pathToServersCfg) throws IOException, NotaryMiddlewareException {

        if (!(BYZANTINE_F < (REPLICAS_N / 3))) {
            throw new NotaryMiddlewareException(String.format("It is not possible to have Byzantine Quorum with N = %d and f = %d", REPLICAS_N, BYZANTINE_F));
        }

        servers = new ArrayList<>();

        List<String> urls = CFGHelper.fetchURLsFromCfg(pathToServersCfg,REPLICAS_N);

        for (String url : urls) {
            try {
                servers.add((NotaryInterface) Naming.lookup(url));
            } catch (NotBoundException e) {
                throw new NotaryMiddlewareException(":( NotBound on Notary at " + url);
            } catch (MalformedURLException e) {
                throw new NotaryMiddlewareException(":( Malform URL! Cannot find Notary Service at " + url);
            } catch (RemoteException e) {
                throw new NotaryMiddlewareException(":( It looks like I miss the connection with Notary at " + url);
            }
        }

        poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(REPLICAS_N);
    }

    @Override
    public Interaction intentionToSell(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        /** create writeList */
        ArrayList<Interaction> writeList = new ArrayList<Interaction>();
        CompletionService<Interaction> completionService =
                new ExecutorCompletionService<Interaction>(poolExecutor);

        /** increment id of current read operation*/
        this.wts++;
        request.setWts(this.wts);
        /** sign here */
        for (NotaryInterface notaryInterface : servers) {
            completionService.submit(new NotaryTask(notaryInterface, NotaryTask.Operation.INTENTION2SELL, request));
        }

        int received = 0;
        boolean errors = false;

        while(received < byzantine_quorum && !errors) {
            Future<Interaction> resultFuture = null;
            try {
                resultFuture = completionService.take(); //blocks if none available

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Interaction result = resultFuture.get();
                /** signature must be verified, if not valid continue */
                writeList.add(result);
                received ++;
            }
            catch(Exception e) {
                //log
                errors = true;
                e.printStackTrace();
            }
        }

        /** here we choose the value with the highest wts from writeList and then clean writeList*/
        /*if(!errors) {
            return this.getHighestTS();
        }

        else {
            return null;
        }*/

        System.out.println("received: "+received);

        return writeList.get(0);
    }

    @Override
    public Interaction getStateOfGood(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        /** create readList */
        ArrayList<Interaction> readList = new ArrayList<Interaction>();
        CompletionService<Interaction> completionService =
                new ExecutorCompletionService<Interaction>(poolExecutor);

        /** increment id of current read operation*/
        this.rid++;
        request.setRid(this.rid);
        for (NotaryInterface notaryInterface : servers) {
            completionService.submit(new NotaryTask(notaryInterface, NotaryTask.Operation.GETSTATEOFGOOD, request));
        }

        int received = 0;
        boolean errors = false;

        while(received < byzantine_quorum && !errors) {
            Future<Interaction> resultFuture = null;
            try {
                resultFuture = completionService.take(); //blocks if none available

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Interaction result = resultFuture.get();
                /** signature must be verified, if not valid continue */
                readList.add(result);
                received ++;
            }
            catch(Exception e) {
                //log
                errors = true;
                e.printStackTrace();
            }
        }

        /** here we choose the value with the highest wts from readlist and then clean readList*/
        /*if(!errors) {
            return this.getHighestTS();
        }

        else {
            return null;
        }*/


        System.out.println("received: "+received);

        return readList.get(0);

    }
    /** iterate readList and retrieve interaction with highest TS */
    private Interaction getHighestTS() {
        return null;
    }

    @Override
    public Interaction transferGood(Interaction request) throws RemoteException, TransactionException, GoodException, HDSSecurityException {
        /** create writeList */
        ArrayList<Interaction> writeList = new ArrayList<Interaction>();
        CompletionService<Interaction> completionService =
                new ExecutorCompletionService<Interaction>(poolExecutor);

        /** increment id of current read operation*/
        this.wts++;
        request.setWts(this.wts);
        /** sign here */
        for (NotaryInterface notaryInterface : servers) {
            completionService.submit(new NotaryTask(notaryInterface, NotaryTask.Operation.TRANSFERGOOD, request));
        }

        int received = 0;
        boolean errors = false;

        while(received < byzantine_quorum && !errors) {
            Future<Interaction> resultFuture = null;
            try {
                resultFuture = completionService.take(); //blocks if none available

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Interaction result = resultFuture.get();
                /** signature must be verified, if not valid continue */
                writeList.add(result);
                received ++;
            }
            catch(Exception e) {
                //log
                errors = true;
                e.printStackTrace();
            }
        }

        /** here we choose the value with the highest wts from writeList and then clean writeList*/
        /*if(!errors) {
            return this.getHighestTS();
        }

        else {
            return null;
        }*/

        System.out.println("received: "+received);

        return writeList.get(0);
    }

    @Override
    public int getClock(int userID) throws RemoteException {
        int response = -1;

        for (NotaryInterface notaryInterface : servers) {
            response = notaryInterface.getClock(userID);
        }

        return response;
    }

    @Override
    public void doPrint() throws RemoteException {

        for (NotaryInterface notaryInterface : servers) {
            notaryInterface.doPrint();
        }

    }

    /**
     * Terminates the Middleware
     */
    @Override
    public void shutdown() {
        poolExecutor.shutdown();
    }
}
