package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.interfaces.NotaryByzantineInterface;
import pt.ulisboa.tecnico.meic.sec.util.CFGHelper;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class NotaryBizantineService implements NotaryByzantineInterface {
    /** The list of remote objects of the servers **/
    private List<NotaryByzantineInterface> servers;

    /** Thread pool for servers tasks **/
    private ThreadPoolExecutor poolExecutor;

    /** Number of replicas*/
    private final int REPLICAS_N = 3;
    /** Maximum number of byzantine faults*/
    private final int BYZANTINE_F = 0;
    /** byzantine quorum*/
    private int pre_byzantine_quorum;

    public NotaryBizantineService() {
        servers = new ArrayList<>();
        pre_byzantine_quorum = (REPLICAS_N + BYZANTINE_F)/2;


    }
    /*Byzantine service*/
    @Override
    public Good receiveWriteTransfer(int ownerID, int buyerID) throws RemoteException {
        return null;
    }

    @Override
    public Good receiveWriteIntention(boolean state, int goodID) throws RemoteException {
        return null;
    }

    @Override
    public Good receiveReadGetState(int goodID) throws RemoteException {
        return null;
    }

    public void initialization() {
        try {
            List<String> urls = CFGHelper.fetchURLsFromCfg(System.getProperty("project.nameserver.config"),0);
            for (String url : urls) {
                try {
                    servers.add((NotaryByzantineInterface) Naming.lookup(url));
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }  catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            poolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean broadcastReadGetState(int goodID) throws RemoteException {
        List responses = Collections.synchronizedList(new ArrayList<Future<Good>>());

        for (NotaryByzantineInterface notaryByzantineInterface : servers) {
            responses.add(poolExecutor.submit(new NotaryByzantineTask(notaryByzantineInterface,
                    NotaryByzantineTask.Operation.GETSTATEOFGOOD, goodID)));
        }

        while(true) {
            if(responses.size() > pre_byzantine_quorum) {
                System.out.println("out " + responses.size());
                break;
            }
            else {
                System.out.println("not yet " + responses.size());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
        /*try {
            return response.get().isForSell();
        } catch (InterruptedException e) {
            throw new RemoteException(e.getMessage());
        } catch (ExecutionException e) {
            throw new RemoteException(e.getMessage());
        }    */
    }

    boolean broadcastWriteTransfer(int ownerID, int buyerID) throws RemoteException {
        Future<Good> response = null;

        for (NotaryByzantineInterface notaryByzantineInterface : servers) {
            response = poolExecutor.submit(new NotaryByzantineTask(notaryByzantineInterface,
                    NotaryByzantineTask.Operation.TRANSFERGOOD, ownerID,buyerID));
        }
        return false;
        /*try {
            return response.get();
        } catch (InterruptedException e) {
            throw new RemoteException(e.getMessage());
        } catch (ExecutionException e) {
            throw new RemoteException(e.getMessage());
        }*/

    }

    boolean broadcastWriteIntention(boolean state, int goodID) throws RemoteException {
        Future<Good> response = null;

        for (NotaryByzantineInterface notaryByzantineInterface : servers) {
            response = poolExecutor.submit(new NotaryByzantineTask(notaryByzantineInterface,
                    NotaryByzantineTask.Operation.INTENTION2SELL, state,goodID));
        }
        return false;
        /*try {
            return response.get();
        } catch (InterruptedException e) {
            throw new RemoteException(e.getMessage());
        } catch (ExecutionException e) {
            throw new RemoteException(e.getMessage());
        }    */
    }
}
