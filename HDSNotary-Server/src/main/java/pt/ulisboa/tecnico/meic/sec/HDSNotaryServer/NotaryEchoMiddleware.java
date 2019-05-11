package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID;
import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.exceptions.NotaryEchoMiddlewareException;
import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.interfaces.NotaryCommunicationInterface;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.*;

import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.Main.NOTARY_SERVICE_PORT;
import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.Main.USERS_CERTS_FOLDER;
import static pt.ulisboa.tecnico.meic.sec.util.CertificateHelper.*;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.NotaryService.*;

public class NotaryEchoMiddleware extends UnicastRemoteObject implements NotaryInterface {

    // TODO: ArrayList of NotaryInterface with all RMI proxy objects
    private ArrayList<NotaryCommunicationInterface> servers;

    private NotaryService notaryService;

    /** list for echos of all clients */
    protected static ClientEcho[] clientEchos;

    private static final long TIMEOUT_MILI = 5;

    /** flag for init RMI */
    private boolean needInitRMI;

    private String pathToServersCfg;

    private String myUrl;

    /** Thread pool for servers tasks **/
    private ThreadPoolExecutor poolExecutor;

    public NotaryEchoMiddleware(String pathToServersCfg, String myUrl, NotaryService notaryService)
            throws NotaryEchoMiddlewareException, IOException {
        this.servers = new ArrayList<>();
        this.notaryService = notaryService;
        this.clientEchos = new ClientEcho[NUMBER_OF_CLIENTS + 1];

        for (int i = 1; i < NUMBER_OF_CLIENTS + 1; i++) {
            this.clientEchos[i] = new ClientEcho(i);
        }

        servers = new ArrayList<>();
        needInitRMI = true;
        this.pathToServersCfg = pathToServersCfg;
        this.myUrl = myUrl;
        poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_NOTARIES);

    }

    private void initRMI() throws NotaryEchoMiddlewareException {
        List<String> urls;
        try {
            urls = CFGHelper.fetchURLsFromCfg(this.pathToServersCfg, 0);
            for (String url : urls) {
                url = url + "COM";
                try {
                    
                    servers.add((NotaryCommunicationInterface) Naming.lookup(url));
                    System.out.println("Varejeira no init a adicionar: " + url);
                    
                } catch (NotBoundException e) {
                    throw new NotaryEchoMiddlewareException(":( NotBound on Notary at " + url);
                } catch (MalformedURLException e) {
                    throw new NotaryEchoMiddlewareException(":( Malform URL! Cannot find Notary Service at " + url);
                } catch (RemoteException e) {
                    System.out.println(":( It looks like I miss the connection with Notary at " + url + " ignoring...");
                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        System.out.println("Varejeira do initRMI: " + servers.size());
    }

    @Override
    public Interaction intentionToSell(Interaction request)
            throws RemoteException, GoodException, HDSSecurityException {
        CompletionService<Interaction> completionService = new ExecutorCompletionService<Interaction>(poolExecutor);
        if (needInitRMI) {
            needInitRMI = false;
            try {
                initRMI();
            } catch (NotaryEchoMiddlewareException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        int clientId = request.getUserID();

        // verify the client signature
        /*Certification cert = new VirtualCertificate();
        cert.init(new File(
                System.getProperty("project.users.cert.path") + clientId + System.getProperty("project.users.cert.ext"))
                        .getAbsolutePath());

        try {
            if (!Digest.verify(request, cert)) {
                throw new HDSSecurityException("You are not user " + clientId + "!!");
            }
        } catch (NoSuchAlgorithmException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }*/

        ClientEcho clientEcho = clientEchos[clientId];
        if (clientEcho.isSentEcho() == false) {
            final int id = new Integer(Main.NOTARY_ID);
            request.setNotaryID(id);
            System.out.println("Varejeira ID: " + request.getNotaryID());

            //notaryService.debugPrintBCArrays();
            int echoClock = NotaryService.echoCounter[id] + 1;

            request.setEchoClock(echoClock);

            System.out.println("Varejeira Echo Clock: " + request.getEchoClock());

            /*cert = new VirtualCertificate();
            cert.init("", new File(System.getProperty("project.notary.private")).getAbsolutePath());
            try {
                request.setNotaryIDSignature(Digest.createDigest(request.echoString(), cert));
            } catch (NoSuchAlgorithmException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }*/
            clientEcho.setSentEcho(true);
            System.out.println("Varejeira Sent Echo: " + clientEcho.isSentEcho());
            Interaction tmp = request;

            for (int i = 1; i <= this.servers.size(); i++) {
                try {
                    NotaryCommunicationInterface notary = this.servers.get(i - 1);
                    System.out.println(notary);

                    completionService.submit(new NotaryEchoTask(notary, NotaryEchoTask.Operation.ECHO, request));
                    
                    request = tmp;
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                //notaryService.debugPrintBCArrays();
            }
            /*System.out.println("Varejeira Sent Echo 2 all");
            try {
                boolean receivedAllEchos = false, receivedAllReadys = false;

                clientEcho.getLock().lock();
                while (!(clientEcho.getNumberOfQuorumReceivedEchos() > (N + F)/2)) {
                    receivedAllEchos = clientEcho.getQuorumEchos().await(TIMEOUT_MILI, TimeUnit.SECONDS);
                }

                int readyClock = ++NotaryService.readyCounter[Main.NOTARY_ID];
                request.setReadyClock(readyClock);

                if (clientEcho.isSentReady() == false && receivedAllEchos==true) {
                    clientEcho.setSentReady(true);
                    for (NotaryCommunicationInterface notary : this.servers) {
                        notary.ready(clientEcho.getQuorum());
                    }
                }
                
                System.out.println("Varejeira Sent Ready 2 all");

                //Amplification phase!
                clientEcho.getLock().lock();
                if((clientEcho.isSentReady() == false) && (clientEcho.getNumberOfQuorumReceivedReadys() > F)) {
                    clientEcho.setSentReady(true);
                    for (NotaryCommunicationInterface notary : this.servers) {
                         notary.ready(clientEcho.getQuorum());
                    }
                }
                

                System.out.println("Varejeira After amplification");

                clientEcho.getLock().lock();
                while (clientEcho.getNumberOfQuorumReceivedReadys() < (2 * F) && clientEcho.isDelivered()==true) {
                    receivedAllReadys = clientEcho.getQuorumReadys().await(TIMEOUT_MILI, TimeUnit.SECONDS);
                }
                

                //readys timeout expired!
                if (receivedAllReadys == false) {
                    throw new HDSSecurityException("Failed during ready propagation phase :(");
                }
                //only after receiving readys
                synchronized(clientEcho) {
                    clientEcho.setDelivered(true);
                    return notaryService.intentionToSell(clientEcho.getQuorum());                       
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
          */  
        }
        System.out.println("Varejeira do return!"); 
        return null;
    }

    @Override
    public Interaction getStateOfGood(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        if(needInitRMI) {
            needInitRMI = false;
            try {
                initRMI();
            } catch (NotaryEchoMiddlewareException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return notaryService.getStateOfGood(request);
    }

    @Override
    public Interaction transferGood(Interaction request) throws RemoteException, TransactionException, GoodException, HDSSecurityException {
        if(needInitRMI) {
            needInitRMI = false;
            try {
                initRMI();
            } catch (NotaryEchoMiddlewareException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public int getClock(int userID) throws RemoteException {
        return 0;
    }

    @Override
    public void doPrint() throws RemoteException {
        notaryService.doPrint();
    }

    @Override
    public void shutdown() throws RemoteException {
        notaryService.shutdown();
    }
}