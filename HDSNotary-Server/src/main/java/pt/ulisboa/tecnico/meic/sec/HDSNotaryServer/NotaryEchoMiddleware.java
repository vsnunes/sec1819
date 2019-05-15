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
import java.util.concurrent.ConcurrentHashMap;
import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.NotaryService.*;

public class NotaryEchoMiddleware extends UnicastRemoteObject implements NotaryInterface {

    // TODO: ArrayList of NotaryInterface with all RMI proxy objects
    public static ArrayList<NotaryCommunicationInterface> servers;

    private NotaryService notaryService;

    protected volatile static ConcurrentHashMap <String, ClientEcho> clientEchosMap;

    /** list for echos of all clients */
    //protected static ClientEcho[] clientEchos;

    static final long TIMEOUT_SEC = 30;

    /** flag for init RMI */
    private boolean needInitRMI;

    private String pathToServersCfg;

    private String myUrl;

    /** Thread pool for servers tasks **/
    private ThreadPoolExecutor poolExecutor;

    public NotaryEchoMiddleware(String pathToServersCfg, String myUrl, NotaryService notaryService)
            throws NotaryEchoMiddlewareException, IOException {

        this.notaryService = notaryService;
        this.pathToServersCfg = pathToServersCfg;
        this.myUrl = myUrl;
        clientEchosMap = new ConcurrentHashMap<>();
        this.reset();
    }

    private void initRMI() throws NotaryEchoMiddlewareException {
        List<String> urls;
        servers = new ArrayList<>();
        try {
            urls = CFGHelper.fetchURLsFromCfg(this.pathToServersCfg, 0);
            for (String url : urls) {
                url = url + "COM";
                try {

                    servers.add((NotaryCommunicationInterface) Naming.lookup(url));
                    // System.out.println("Varejeira no init a adicionar: " + url);

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
        // System.out.println("Varejeira do initRMI: " + servers.size());
    }

    @Override
    public Interaction intentionToSell(Interaction request)
            throws RemoteException, GoodException, HDSSecurityException {
        CompletionService<Interaction> completionServiceEcho = new ExecutorCompletionService<Interaction>(poolExecutor);
        CompletionService<Interaction> completionServiceReady = new ExecutorCompletionService<Interaction>(poolExecutor);
        System.out.println("MAL RECEBI: " + request.toString());

        int clientId = request.getUserID();
        
        //ClientEcho clientEcho = clientEchos[clientId];
        ClientEcho clientEcho = null;

        String echoIdentifier = String.valueOf(request.getUserID()) + String.valueOf(request.getUserClock());

        System.out.println("ESTOU A USAR A KEY " + echoIdentifier);
        
        synchronized(clientEchosMap){
            if(clientEchosMap.containsKey(echoIdentifier)) {
                clientEcho = clientEchosMap.get(echoIdentifier);
            } else {
                clientEcho = new ClientEcho();
                clientEchosMap.put(echoIdentifier, clientEcho);
            }
        }
        try {
            initRMI();
        } catch (NotaryEchoMiddlewareException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // verify the client signature
        Certification cert = new VirtualCertificate();
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
        }

        if (clientEcho.isSentReady()) {
            throw new HDSSecurityException("Amplification phase already triggered, ignoring request!");
        }

        if (clientEcho.isSentEcho() == false) {
            final int id = new Integer(Main.NOTARY_ID);
            request.setNotaryID(id);
            // System.out.println("Varejeira ID: " + request.getNotaryID());

            // notaryService.debugPrintBCArrays();
            int echoClock = NotaryService.echoCounter[id][clientId] + 1;

            request.setEchoClock(echoClock);

            // System.out.println("Varejeira Echo Clock: " + request.getEchoClock());

            cert = new VirtualCertificate();
            cert.init("", new File(System.getProperty("project.notary.private")).getAbsolutePath());
            try {
                request.setNotaryIDSignature(Digest.createDigest(request.echoString(), cert));
            } catch (NoSuchAlgorithmException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            clientEcho.setSentEcho(true);
            // System.out.println("Varejeira Sent Echo: " + clientEcho.isSentEcho());
            Interaction tmp = request;

            for (int i = 1; i <= this.servers.size(); i++) {
                try {
                    NotaryCommunicationInterface notary = this.servers.get(i - 1);

                    completionServiceEcho.submit(new NotaryEchoTask(notary, NotaryEchoTask.Operation.ECHO, request));

                    request = tmp;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            System.out.println("Varejeira Sent Echo 2 all");
            try {
                boolean receivedAllEchos = false, receivedAllReadys = false;

                /*int quorumEchos = 0;
                Future<Interaction> resultFuture = null;
                while (quorumEchos < NUMBER_OF_NOTARIES) {
                    resultFuture = completionService.poll(TIMEOUT_SEC, TimeUnit.SECONDS);
                    if (resultFuture == null) {
                        System.out.println("Não recebi o quorum de echos!!!! Timeout disparado");
                        throw new HDSSecurityException("Não recebi o quorum de echos!!!!");
                    }
                    quorumEchos++;
                }*/
                System.out.println("Before quorum echo middleware " + clientEcho.getNumberOfQuorumReceivedEchos());
                int waited = 0;
                while (clientEcho.getNumberOfQuorumReceivedEchos() <= ((N + F) / 2)) {
                    Thread.sleep(500);
                    System.out.println("After ECHO sleep " + clientEcho.getNumberOfQuorumReceivedEchos() + " ID " + echoIdentifier);
                    waited++; 
                    if (waited >= 200) { 
                        System.out.println("Timeout expired on echos");
                    throw new HDSSecurityException("Timeout expired on echos"); 
                    }
                     
                }

                System.out.println("After quorum echo middleware " + clientEcho.getNumberOfQuorumReceivedEchos());

                request = clientEcho.getQuorumEchos();
                final int idNotary = new Integer(Main.NOTARY_ID);
                request.setNotaryID(idNotary);
                int readyClock = NotaryService.readyCounter[idNotary][clientId] + 1;
                request.setReadyClock(readyClock);
                request.setType(Interaction.Type.INTENTION2SELL);

                cert = new VirtualCertificate();
                cert.init("", new File(System.getProperty("project.notary.private")).getAbsolutePath());
                try {
                    request.setReadySignature(Digest.createDigest(request.readyString(), cert));
                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                }
                System.out.println(
                        Main.NOTARY_ID + " i'm about to send readys " + clientEcho.isSentReady() + receivedAllEchos);
                if (clientEcho.isSentReady() == false) {
                    clientEcho.setSentReady(true);
                    for (NotaryCommunicationInterface notary : this.servers) {
                        try {
                            completionServiceReady.submit(new NotaryEchoTask(notary, NotaryEchoTask.Operation.READY, request));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println("Varejeira Sent Ready 2 all");

                if (clientEcho.isDelivered() == false) {
                    /*int quorumReadys = 0;
                    resultFuture = null;
                    while (quorumReadys < NUMBER_OF_NOTARIES) {
                        resultFuture = completionService.poll(TIMEOUT_SEC, TimeUnit.SECONDS);
                        if (resultFuture == null) {
                            System.out.println("Not received quorum of readys! Timeout disparado");
                            throw new HDSSecurityException("Not received quorum of readys!");
                        }
                        quorumReadys++;
                    }*/

                    waited = 0;
                    while (clientEcho.getNumberOfQuorumReceivedReadys() <= (2 * F)) {
                        Thread.sleep(500);
                        System.out.println("After READY sleep " + clientEcho.getNumberOfQuorumReceivedReadys() + " ID " + echoIdentifier);
                        waited++; 
                        if (waited >= 200) {
                            System.out.println("Timeout expired on readys"); 
                            throw new HDSSecurityException("Timeout expired on readys"); 
                        }
                        
                    }

                    request = clientEcho.getQuorumReadys();
                    request.setNotaryID(idNotary);
                    request.setType(Interaction.Type.INTENTION2SELL);

                    // only after receiving readys
                    synchronized (clientEcho) {
                        clientEcho.setDelivered(true);
                        System.out.println("ANTES: " + request.toString());
                        //clientEchosMap.remove(echoIdentifier);
                        return notaryService.intentionToSell(request);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("Varejeira isSent is True");
        }
        System.out.println("Varejeira do return!");
        return null;
    }

    public void reset() {
        /*this.clientEchos = new ClientEcho[NUMBER_OF_CLIENTS + 1];

        for (int i = 1; i <= NUMBER_OF_CLIENTS; i++) {
            this.clientEchos[i] = new ClientEcho(i);
        }*/

        servers = new ArrayList<>();
        needInitRMI = true;
        poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_NOTARIES);
        /*try {
            System.out.println("Adormeci para limpar, não deveria estar a receber pedidos");
            Thread.sleep(10000);
            System.out.println("Acordei, já posso receber pedidos!");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
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
        return this.notaryService.getClock(userID);
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