package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID;
import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.exceptions.NotaryEchoMiddlewareException;
import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.interfaces.NotaryCommunicationInterface;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.*;
import pt.ulisboa.tecnico.meic.sec.util.Interaction.Type;

import javax.xml.bind.DatatypeConverter;

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

    protected volatile static ConcurrentHashMap<String, ClientEcho> clientEchosMap;

    /** list for echos of all clients */
    // protected static ClientEcho[] clientEchos;

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
    }

    @Override
    public Interaction intentionToSell(Interaction request)
            throws RemoteException, GoodException, HDSSecurityException {
        CompletionService<Interaction> completionServiceEcho = new ExecutorCompletionService<Interaction>(poolExecutor);
        CompletionService<Interaction> completionServiceReady = new ExecutorCompletionService<Interaction>(
                poolExecutor);

        System.out.println(" Recebi o pedido: " + request.toString());


        int clientId = request.getUserID();

        // ClientEcho clientEcho = clientEchos[clientId];
        ClientEcho clientEcho = null;

        String echoIdentifier = "ITS" + String.valueOf(request.getUserID()) + String.valueOf(request.getUserClock());
        synchronized (clientEchosMap) {
            if (clientEchosMap.containsKey(echoIdentifier)) {
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

        if (clientEcho.isDelivered()) {
            System.out.println("This message was already delivered in the application");
            throw new HDSSecurityException("This message was already delivered in the application");
        }

        if (clientEcho.isSentReady()) {
            System.out.println("Amplification phase already triggered, ignoring request!");
            throw new HDSSecurityException("Amplification phase already triggered, ignoring request!");
        }

        if (clientEcho.isSentEcho() == false) {
            final int id = new Integer(Main.NOTARY_ID);
            request.setNotaryID(id);

            int echoClock = NotaryService.echoCounter[id][clientId] + 1;

            request.setEchoClock(echoClock);
            request.setType(Interaction.Type.INTENTION2SELL);

            cert = new VirtualCertificate();
            cert.init("", new File(System.getProperty("project.notary.private")).getAbsolutePath());
            try {
                request.setNotaryIDSignature(Digest.createDigest(request.echoString(), cert));
            } catch (NoSuchAlgorithmException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
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

            boolean receivedAllEchos = false, receivedAllReadys = false;

            int waited = 0;
            while (clientEcho.getNumberOfQuorumReceivedEchos() <= ((N + F) / 2)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                waited++;
                if (waited >= 20) {
                    System.out.println("Timeout expired on echos");
                    throw new HDSSecurityException("Timeout expired on echos");
                }

            }

            clientEcho.setSentEcho(true);
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
            if (clientEcho.isSentReady() == false) {
                for (NotaryCommunicationInterface notary : this.servers) {
                    try {
                        completionServiceReady
                                .submit(new NotaryEchoTask(notary, NotaryEchoTask.Operation.READY, request));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (clientEcho.isDelivered() == false) {

                waited = 0;
                while ((clientEcho.getNumberOfQuorumReceivedReadys() <= (2 * F)) && (!clientEcho.isDelivered())) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                        waited++;
                        if (waited >= 200) {
                            System.out.println("Timeout expired on readys"); 
                            throw new HDSSecurityException("Timeout expired on readys"); 
                        }
                    }
                    clientEcho.setSentReady(true);

                    if(!clientEcho.isDelivered()) {
                        if(clientEcho.getDeliveredLock().tryLock()) {
                            clientEcho.setDelivered(true);
                            request = clientEcho.getQuorumReadys();
                            request.setNotaryID(idNotary);
                            request.setType(Interaction.Type.INTENTION2SELL);

                            // only after receiving readys
                            synchronized (clientEcho) {
                                clientEcho.setDelivered(true);
                                try {
                                    return notaryService.intentionToSell(request);
                                }
                                finally {
                                    clientEcho.getDeliveredLock().unlock();
                                }
                            }
                        }
                    }

                }

        }

        return null;
    }

    public void reset() {

        servers = new ArrayList<>();
        needInitRMI = true;
        poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_NOTARIES);

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
        CompletionService<Interaction> completionServiceEcho = new ExecutorCompletionService<Interaction>(poolExecutor);
        CompletionService<Interaction> completionServiceReady = new ExecutorCompletionService<Interaction>(poolExecutor);

        int clientId = request.getSellerID();

        int nounce  = request.getNounce();
        byte[] proofOfWork = request.getProofOfWork();

        System.out.println(DatatypeConverter.printBase64Binary(proofOfWork));
        if(!Arrays.equals(proofOfWork, ProofOfWork.calculateWithNounce("2", request.toStringPOW(), nounce)) || (!ProofOfWork.verifyProof(proofOfWork, 3))){
            System.out.println("Proof of Work not valid");
            throw new HDSSecurityException("Proof of Work not valid");
        }
        //ClientEcho clientEcho = clientEchos[clientId];
        ClientEcho clientEcho = null;

        String echoIdentifier = "TG" + String.valueOf(request.getBuyerID()) + String.valueOf(request.getBuyerClock()) + String.valueOf(request.getSellerID()) + String.valueOf(request.getSellerClock());


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

        System.out.println("cert init");

        try {
            String data = "" + request.getSellerID() + request.getBuyerID() + request.getGoodID() + request.getSellerClock() + request.getBuyerClock();
            if(!Digest.verify(request.getSellerHMAC(), data, cert)){
                System.out.println("You are not user " + clientId + "!!");
                throw new HDSSecurityException("You are not user " + clientId + "!!");
            }
        } catch (Exception e2) {
            System.out.println("NoSuchAlgorithm verify request failed\n" + e2.getMessage());
        }


        if(clientEcho.isDelivered()) {
            System.out.println("This message was already delivered in the application");
            throw new HDSSecurityException("This message was already delivered in the application");
        }

        if (clientEcho.isSentReady()) {
            System.out.println("Amplification phase already triggered, ignoring request!");
            throw new HDSSecurityException("Amplification phase already triggered, ignoring request!");
        }

        if (clientEcho.isSentEcho() == false) {
            final int id = new Integer(Main.NOTARY_ID);
            request.setNotaryID(id);
            request.setType(Interaction.Type.TRANSFERGOOD);

            int echoClock = NotaryService.echoCounter[id][clientId] + 1;

            request.setEchoClock(echoClock);

            cert = new VirtualCertificate();
            cert.init("", new File(System.getProperty("project.notary.private")).getAbsolutePath());
            try {
                request.setNotaryIDSignature(Digest.createDigest(request.echoString(), cert));
            } catch (NoSuchAlgorithmException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
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
            clientEcho.setSentEcho(true);
            try {
                boolean receivedAllEchos = false, receivedAllReadys = false;

                System.out.println("Before quorum echo middleware " + clientEcho.getNumberOfQuorumReceivedEchos());
                int waited = 0;
                while (clientEcho.getNumberOfQuorumReceivedEchos() <= ((N + F) / 2)) {
                    Thread.sleep(500);
                    waited++;
                    if (waited >= 200) { 
                        System.out.println("Timeout expired on echos");
                        throw new HDSSecurityException("Timeout expired on echos"); 
                    }
                     
                }

                request = clientEcho.getQuorumEchos();
                final int idNotary = new Integer(Main.NOTARY_ID);
                request.setNotaryID(idNotary);
                int readyClock = NotaryService.readyCounter[idNotary][clientId] + 1;
                request.setReadyClock(readyClock);
                request.setType(Interaction.Type.TRANSFERGOOD);

                cert = new VirtualCertificate();
                cert.init("", new File(System.getProperty("project.notary.private")).getAbsolutePath());
                try {
                    request.setReadySignature(Digest.createDigest(request.readyString(), cert));
                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                }
                if (clientEcho.isSentReady() == false) {
                    for (NotaryCommunicationInterface notary : this.servers) {
                        try {
                            completionServiceReady.submit(new NotaryEchoTask(notary, NotaryEchoTask.Operation.READY, request));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (clientEcho.isDelivered() == false) {

                    waited = 0;
                    while ((clientEcho.getNumberOfQuorumReceivedReadys() <= (2 * F)) && (!clientEcho.isDelivered())) {
                        Thread.sleep(500);
                        waited++;
                        if (waited >= 200) {
                            System.out.println("Timeout expired on readys"); 
                            throw new HDSSecurityException("Timeout expired on readys"); 
                        }
                    }
                    clientEcho.setSentReady(true);

                    if(!clientEcho.isDelivered()) {
                        if(clientEcho.getDeliveredLock().tryLock()) {
                            clientEcho.setDelivered(true);
                            request = clientEcho.getQuorumReadys();
                            request.setNotaryID(idNotary);
                            request.setType(Interaction.Type.TRANSFERGOOD);

                            // only after receiving readys
                            synchronized (clientEcho) {
                                clientEcho.setDelivered(true);
                                try {
                                    return notaryService.transferGood(request);
                                }
                                finally {
                                    clientEcho.getDeliveredLock().unlock();
                                }
                            }
                        }
                    }

                    //this part is just to prevent null pointer exception client side
                }

            } catch (Exception e) {
                //e.printStackTrace();
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