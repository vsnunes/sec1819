package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryClient.exceptions.NotaryMiddlewareException;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

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
    private static final int REPLICAS_N = 3;

    /** Maximum number of Byzantine faults
     * the maximum number of faults that may occur while preserving the correctness of the HDS Notary system
     **/
    private static final int BYZANTINE_F = 0;

    /** The list of remote objects of the servers **/
    private List<NotaryInterface> servers;

    /** Thread pool for servers tasks **/
    private ThreadPoolExecutor poolExecutor;

    public NotaryMiddleware(String pathToServersCfg) throws IOException, NotaryMiddlewareException {

        if (!(BYZANTINE_F < (REPLICAS_N / 3))) {
            throw new NotaryMiddlewareException(String.format("It is not possible to have Byzantine Quorum with N = %d and f = %d", REPLICAS_N, BYZANTINE_F));
        }

        servers = new ArrayList<>();

        List<String> urls = fetchURLsFromCfg(pathToServersCfg);

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

        poolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    @Override
    public Interaction intentionToSell(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        Future<Interaction> response = null;

        for (NotaryInterface notaryInterface : servers) {
            response = poolExecutor.submit(new NotaryTask(notaryInterface, NotaryTask.Operation.INTENTION2SELL, request));
        }

        try {
            return response.get();
        } catch (InterruptedException e) {
            throw new RemoteException(e.getMessage());
        } catch (ExecutionException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public Interaction getStateOfGood(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        Future<Interaction> response = null;

        for (NotaryInterface notaryInterface : servers) {
            response = poolExecutor.submit(new NotaryTask(notaryInterface, NotaryTask.Operation.GETSTATEOFGOOD, request));
        }

        try {
            return response.get();
        } catch (InterruptedException e) {
            throw new RemoteException(e.getMessage());
        } catch (ExecutionException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public Interaction transferGood(Interaction request) throws RemoteException, TransactionException, GoodException, HDSSecurityException {
        Future<Interaction> response = null;

        for (NotaryInterface notaryInterface : servers) {
            response = poolExecutor.submit(new NotaryTask(notaryInterface, NotaryTask.Operation.TRANSFERGOOD, request));
        }

        try {
            return response.get();
        } catch (InterruptedException e) {
            throw new RemoteException(e.getMessage());
        } catch (ExecutionException e) {
            throw new RemoteException(e.getMessage());
        }
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
     * Given a path to the Servers.cfg returns a list of servers URLs
     * @param pathToServersCfg path to Servers.cfg
     * @return list of urls containing the servers URLs.
     */
    private List<String> fetchURLsFromCfg(String pathToServersCfg) throws IOException {
        FileReader fileReader = new FileReader(pathToServersCfg);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        List<String> urls = new ArrayList<>();
        String url;

        for (int i = 1; (url = bufferedReader.readLine()) != null; i++) {
            if ((url != "") && ((REPLICAS_N == 0) || (i <= REPLICAS_N))) urls.add(url);
        }
        System.out.println(String.format("** NotaryMiddleware: Found %d url(s) of servers!", urls.size()));
        return urls;
    }

    /**
     * Terminates the Middleware
     */
    @Override
    public void shutdown() {
        poolExecutor.shutdown();
    }
}
