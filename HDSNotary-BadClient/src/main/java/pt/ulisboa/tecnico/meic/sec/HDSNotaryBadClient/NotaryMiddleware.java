package pt.ulisboa.tecnico.meic.sec.HDSNotaryBadClient;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryBadClient.exceptions.NotaryMiddlewareException;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

    /**timeout for responses waiting */
    private final int TIMEOUT = 30;

    /**flag to test sending requests only to some notaries */
    public static int notariesToSend = 4;


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
                System.out.println(":( It looks like I miss the connection with Notary at " + url + " ignoring...");
                //throw new NotaryMiddlewareException(":( It looks like I miss the connection with Notary at " + url);
            }
        }

        poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(REPLICAS_N);
    }

    @Override
    public Interaction intentionToSell(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        /** create writeList */
        ArrayList<Interaction> writeList = new ArrayList<>();
        CompletionService<Interaction> completionService =
                new ExecutorCompletionService<>(poolExecutor);

        /** increment id of current read operation*/
        request.setWts(request.getWts()+1);
        /** sign here */
        Certification cert = new VirtualCertificate();
        cert.init("", new File(System.getProperty("project.user.private.path") +
                ClientService.userID + System.getProperty("project.user.private.ext")).getAbsolutePath());

        /*prepare request arguments*/
        try {
                request.setSigma(Digest.createDigest(""+request.getWts()+request.getResponse(), cert));
            int toSend = 0;
            for (NotaryInterface notaryInterface : servers) {
                if(toSend == this.notariesToSend) {
                    break;
                }
                completionService.submit(new NotaryTask(notaryInterface, NotaryTask.Operation.INTENTION2SELL, request));
                toSend++;
            }
            this.notariesToSend = 4;

            int received = 0;
            boolean errors = false;

            while(received < byzantine_quorum && !errors) {
                Future<Interaction> resultFuture = null;
                try {
                    resultFuture = completionService.poll(TIMEOUT,TimeUnit.SECONDS); //blocks if none available
                    if(resultFuture == null) {
                        throw new GoodException("Byzantine quorum not achieved :(");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Interaction result = resultFuture.get();
                    writeList.add(result);
                    received ++;
                }
                catch(ExecutionException e) {
                    //log
                    errors = true;
                    //e.printStackTrace();
                }
                catch(InterruptedException e) {
                    //log
                    errors = true;
                    //e.printStackTrace();
                }
            }

            /** here we choose the value with the highest wts from writeList and then clean writeList*/
            if(!errors) {
                System.out.println("received: "+received);
                return this.getHighestTS(writeList);
            }

            else {
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    /** iterate readList and retrieve interaction with highest TS */
    private Interaction getHighestTS(ArrayList<Interaction> list) {
        Interaction fresherOne = null;
        for(Interaction interaction:list) {
            if(fresherOne == null) {
                fresherOne = interaction;
            }
            else {
                if(interaction.getWts() > fresherOne.getWts()) {
                    fresherOne = interaction;
                }
            }
        }
        return fresherOne;
    }

    @Override
    public Interaction getStateOfGood(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        /** create readList */
        ArrayList<Interaction> readList = new ArrayList<Interaction>();
        CompletionService<Interaction> completionService =
                new ExecutorCompletionService<Interaction>(poolExecutor);
        for (NotaryInterface notaryInterface : servers) {
            completionService.submit(new NotaryTask(notaryInterface, NotaryTask.Operation.GETSTATEOFGOOD, request));
        }
        this.notariesToSend = 4;


        int received = 0;
        boolean errors = false;

        while(received < byzantine_quorum && !errors) {
            Future<Interaction> resultFuture = null;
            try {
                resultFuture = completionService.poll(TIMEOUT, TimeUnit.SECONDS); //blocks if none available
                if(resultFuture == null) {
                    throw new GoodException("Byzantine quorum not achieved :(");
                }

            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
            try {
                Interaction result = resultFuture.get();
                VirtualCertificate clientCert = new VirtualCertificate();

                if(result.getSigma()==null) {
                    throw new GoodException("To read you need to write something first");
                }
                else {
                    System.out.println("owner: " + result.getOwnerID());
                    clientCert.init(new File(System.getProperty("project.users.cert.path") + result.getOwnerID() + System.getProperty("project.users.cert.ext")).getAbsolutePath());
                    if(Digest.verify(result.getSigma(),""+result.getWts()+result.getResponse(), clientCert) == false) {
                        continue;
                    }
                }


                readList.add(result);
                received ++;
            }
            catch(InterruptedException e) {
                //log
                errors = true;
                e.printStackTrace();
            }
            catch(ExecutionException e) {
                //log
                errors = true;
                //e.printStackTrace();
            }
            catch(NoSuchAlgorithmException e) {
                //log
                errors = true;
                e.printStackTrace();
            }
        }

        /** here we choose the value with the highest wts from readlist and then clean readList*/
        if(!errors) {
            System.out.println("received: "+received);
            Interaction mostRecent = this.getHighestTS(readList);
            
            Interaction newRequest = new Interaction();

            if (mostRecent.getType() == Interaction.Type.INTENTION2SELL) {
                /*prepare request arguments*/
                newRequest.setUserID(mostRecent.getOwnerID());
                newRequest.setGoodID(mostRecent.getGoodID());
                newRequest.setResponse(mostRecent.getResponse());
                newRequest.setUserClock(mostRecent.getOwnerClock());
                newRequest.setHmac(mostRecent.getLastChangeHMAC());
                newRequest.setWts(mostRecent.getWts());
                newRequest.setSigma(mostRecent.getSigma());
                
                /** perform the write */
                System.out.println("Performing write back phase!");
                this.intentionToSell(newRequest);
            } else if (mostRecent.getType() == Interaction.Type.TRANSFERGOOD) {

                newRequest.setBuyerID(mostRecent.getBuyerID());
                newRequest.setSellerID(mostRecent.getSellerID());
                newRequest.setSellerClock(mostRecent.getSellerClock());
                newRequest.setBuyerClock(mostRecent.getBuyerClock());

                newRequest.setGoodID(mostRecent.getGoodID());
                newRequest.setResponse(mostRecent.getResponse());

                newRequest.setBuyerHMAC(mostRecent.getLastChangeHMAC());
                newRequest.setSellerHMAC(mostRecent.getLastChangeHMACSeller());
                newRequest.setWts(mostRecent.getWts());
                newRequest.setSigma(mostRecent.getSigma());
                try {
                    System.out.println("Performing write back phase!");
                    this.transferGood(newRequest);
                } catch (TransactionException e) {
                    e.printStackTrace();
                }
            }
            return mostRecent;

        }
        else {
            return null;
        }


    }


    @Override
    public Interaction transferGood(Interaction request) throws RemoteException, TransactionException, GoodException, HDSSecurityException {
        /** create writeList */
        ArrayList<Interaction> writeList = new ArrayList<Interaction>();
        CompletionService<Interaction> completionService =
                new ExecutorCompletionService<Interaction>(poolExecutor);

        /*prepare request arguments*/
        int toSend = 0;
        for (NotaryInterface notaryInterface : servers) {
            if(toSend == this.notariesToSend) {
                break;
            }
            completionService.submit(new NotaryTask(notaryInterface, NotaryTask.Operation.TRANSFERGOOD, request));
            toSend++;
        }

        int received = 0;
        boolean errors = false;

        while(received < byzantine_quorum && !errors) {
            Future<Interaction> resultFuture = null;
            try {
                resultFuture = completionService.poll(TIMEOUT, TimeUnit.SECONDS); //blocks if none available
                if(resultFuture == null) {
                    return null;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Interaction result = resultFuture.get();
                writeList.add(result);
                received ++;
            } catch (InterruptedException e) {
                errors = true;
                e.printStackTrace();
            } catch (ExecutionException e) {
                errors = true;
                //e.printStackTrace();
            }

        }

        /** here we choose the value with the highest wts from writeList and then clean writeList*/
        if(!errors) {
            System.out.println("received: "+received);
            return this.getHighestTS(writeList);
        }

        else {
            return null;
        }

    }

    @Override
    public int getClock(int userID) throws RemoteException {
        int maxResponse = -1, response;

        for (NotaryInterface notaryInterface : servers) {
            response = notaryInterface.getClock(userID);
            if (response > maxResponse) {
                maxResponse = response;
            }
        }

        return maxResponse;
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
