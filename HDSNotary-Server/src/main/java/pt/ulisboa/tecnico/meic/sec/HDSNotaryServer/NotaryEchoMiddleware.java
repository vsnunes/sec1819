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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.NotaryService.*;

public class NotaryEchoMiddleware implements NotaryInterface {

    //TODO: ArrayList of NotaryInterface with all RMI proxy objects
    private ArrayList<NotaryCommunicationInterface> servers;

    private NotaryService notaryService;

    /** list for echos of all clients */
    protected static volatile ArrayList<ClientEcho> clientEchos;

    private static final long TIMEOUT_MILI = 5;


    public NotaryEchoMiddleware(String pathToServersCfg, String myUrl, NotaryService notaryService) throws NotaryEchoMiddlewareException, IOException {
        this.servers = new ArrayList<>();
        this.notaryService = notaryService;
        this.clientEchos = new ArrayList<ClientEcho>(NUMBER_OF_CLIENTS);
        servers = new ArrayList<>();

        List<String> urls = CFGHelper.fetchURLsFromCfg(pathToServersCfg,0);

        for (String url : urls) {
            try {
                if(!url.equals(myUrl)) {
                    servers.add((NotaryCommunicationInterface) Naming.lookup(url));
                }
            } catch (NotBoundException e) {
                throw new NotaryEchoMiddlewareException(":( NotBound on Notary at " + url);
            } catch (MalformedURLException e) {
                throw new NotaryEchoMiddlewareException(":( Malform URL! Cannot find Notary Service at " + url);
            } catch (RemoteException e) {
                System.out.println(":( It looks like I miss the connection with Notary at " + url + " ignoring...");
            }
        }
    }

    @Override
    public Interaction intentionToSell(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        //verify the client signature
        int clientId = request.getClientID();
        
        
        ClientEcho clientEcho = clientEchos.get(clientId);
        if (clientEcho.isSentEcho() == false) {
            request.setNotaryID(Main.NOTARY_ID);

            int echoClock = ++NotaryService.echoCounter[Main.NOTARY_ID];
            request.setEchoClock(echoClock);

            Certification cert = new VirtualCertificate();
            cert.init("", new File(System.getProperty("project.notary.private")).getAbsolutePath());
            try {
                request.setNotaryIDSignature(Digest.createDigest(request.echoString(), cert));
            } catch (NoSuchAlgorithmException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            clientEcho.setSentEcho(true);

            for (NotaryCommunicationInterface notary : this.servers) {
                notary.echo(request);
            }
            
            try {
                boolean receivedAllEchos = false, receivedAllReadys = false;

                synchronized(clientEcho.getQuorumEchos()) {
                    while (!(clientEcho.getNumberOfQuorumReceivedEchos() > (N + F)/2)) {
                        receivedAllEchos = clientEcho.getQuorumEchos().await(TIMEOUT_MILI, TimeUnit.SECONDS);
                    }
                }


                int readyClock = ++NotaryService.readyCounter[Main.NOTARY_ID];
                request.setReadyClock(readyClock);

                if (clientEcho.isSentReady() == false && receivedAllEchos==true) {
                    clientEcho.setSentReady(true);
                    for (NotaryCommunicationInterface notary : this.servers) {
                        notary.ready(clientEcho.getQuorum());
                    }
                }

                //Amplification phase!
                synchronized(clientEcho.getQuorumReadys()) {
                    if((clientEcho.isSentReady() == false) && (clientEcho.getNumberOfQuorumReceivedReadys() > F)) {
                        clientEcho.setSentReady(true);
                        for (NotaryCommunicationInterface notary : this.servers) {
                            notary.ready(clientEcho.getQuorum());
                        }
                    }
                }
                
                synchronized(clientEcho.getQuorumReadys()) {
                    while (clientEcho.getNumberOfQuorumReceivedReadys() < (2 * F) && clientEcho.isDelivered()==true) {
                        receivedAllReadys = clientEcho.getQuorumReadys().await(TIMEOUT_MILI, TimeUnit.SECONDS);
                    }
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
            
        }
            
        return null;
    }

    @Override
    public Interaction getStateOfGood(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        return notaryService.getStateOfGood(request);
    }

    @Override
    public Interaction transferGood(Interaction request) throws RemoteException, TransactionException, GoodException, HDSSecurityException {
        return null;
    }

    @Override
    public int getClock(int userID) throws RemoteException {
        return 0;
    }

    @Override
    public void doPrint() throws RemoteException {

    }

    @Override
    public void shutdown() throws RemoteException {

    }
}