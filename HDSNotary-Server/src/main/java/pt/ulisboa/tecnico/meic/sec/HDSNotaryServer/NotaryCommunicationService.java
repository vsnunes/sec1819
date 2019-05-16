package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.exceptions.NotaryEchoMiddlewareException;
import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.interfaces.NotaryCommunicationInterface;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.util.CFGHelper;
import pt.ulisboa.tecnico.meic.sec.util.Certification;
import pt.ulisboa.tecnico.meic.sec.util.Digest;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;
import pt.ulisboa.tecnico.meic.sec.util.VirtualCertificate;
import pt.ulisboa.tecnico.meic.sec.util.Interaction.Type;

import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.NotaryService.*;
import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.NotaryEchoMiddleware.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NotaryCommunicationService extends UnicastRemoteObject
        implements NotaryCommunicationInterface, Serializable {

    private ThreadPoolExecutor poolExecutor;

    protected NotaryCommunicationService() throws RemoteException {
        super();
        poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_NOTARIES);
    }

    @Override
    public void echo(Interaction request) throws RemoteException {

        System.out.println("Varejeira: recebi echo do " + request.getNotaryID());
        int clientId = request.getUserID();
        int notaryId = request.getNotaryID();
        int lastEchoCounter = -1;

        // verify the client signature
        Certification cert = new VirtualCertificate();
        try {
            cert.init(new File(System.getProperty("project.users.cert.path") + clientId
                    + System.getProperty("project.users.cert.ext")).getAbsolutePath());
        } catch (HDSSecurityException e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
        }

        try {
            if (!Digest.verify(request, cert)) {
                throw new RemoteException("You are not user " + clientId + "!!");
            }
        } catch (NoSuchAlgorithmException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (HDSSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        synchronized (NotaryService.echoCounter) {
            lastEchoCounter = NotaryService.echoCounter[notaryId][clientId];
        }

        // System.out.println("varejeira lastEchoCounter " + lastEchoCounter);
        // System.out.println("varejeira echoClock " + request.getEchoClock());
        if (request.getEchoClock() <= lastEchoCounter) {
            System.out.println("Replay attack of echo message! vindo do notario " + notaryId);
            throw new RemoteException("Replay attack of echo message! vindo do notario " + notaryId);
        }

        synchronized (NotaryService.echoCounter) {
            NotaryService.echoCounter[notaryId][clientId] = new Integer(request.getEchoClock());
            NotaryService.doWriteRB();
            System.out.println("recebi o echo do " + request.getNotaryID() + " e realizei a escrita persistente");
        }

        // System.out.println("Varejeira after checking echo clock");
        /*
         * ClientEcho clientEcho = null; synchronized (NotaryEchoMiddleware.clientEchos)
         * { clientEcho = NotaryEchoMiddleware.clientEchos[clientId]; }
         */

        // if this request exists on echoHashMap
        ClientEcho clientEcho = null;

        String echoIdentifier = "";
        if(request.getType()==Interaction.Type.INTENTION2SELL) {
            echoIdentifier = String.valueOf(request.getUserID()) + String.valueOf(request.getUserClock());
            synchronized (clientEchosMap) {
                if (clientEchosMap.containsKey(echoIdentifier)) {
                    clientEcho = clientEchosMap.get(echoIdentifier);
                } else {
                    clientEcho = new ClientEcho();
                    clientEchosMap.put(echoIdentifier, clientEcho);
                }
            }
        } else {
            System.out.println("ECHO ASNEIRA!!!!!!!!!!!!!!!!!!!!");
        }

        VirtualCertificate notaryCert = new VirtualCertificate();
        try {
            notaryCert.init(new File("../HDSNotaryLib/src/main/resources/certs/notary" + request.getNotaryID() + ".crt")
                    .getAbsolutePath());
        } catch (HDSSecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        // System.out.println("Varejeira after cert");
        // compare hmacs
        try {
            if (Digest.verify(request.getNotaryIDSignature(), request.echoString(), notaryCert) == false) {
                throw new HDSSecurityException("tampering detected in echo message!");
            }
        } catch (NoSuchAlgorithmException | HDSSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // System.out.println("Varejeira after sign");

        // if (notaryInteraction == null) {
        // System.out.println("Varejeira after if notaryInteraction a null");
        clientEcho.addEcho(request);
        System.out.println("FILIPE: ECHO recebi este request " + request.toString() + " do notario "
                + request.getNotaryID() + " e o ID " + echoIdentifier + " e estou com "
                + clientEcho.getNumberOfQuorumReceivedEchos() + " valores iguais no array");

        System.out.println("Varejeira: sai do echo do " + request.getNotaryID() + "**saida**");
        // System.out.println("Varejeira leaving echo function");
    }

    @Override
    public void ready(Interaction request) throws RemoteException {
        System.out.println("Varejeira: recebi ready do " + request.getNotaryID());
        int clientId = request.getUserID();
        int notaryId = request.getNotaryID();
        int lastReadyCounter = -1;

        // verify the client signature
        Certification cert = new VirtualCertificate();
        try {
            cert.init(new File(System.getProperty("project.users.cert.path") + clientId
                    + System.getProperty("project.users.cert.ext")).getAbsolutePath());
        } catch (HDSSecurityException e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
        }

        try {
            if (!Digest.verify(request, cert)) {
                throw new RemoteException("You are not user " + clientId + "!!");
            }
        } catch (NoSuchAlgorithmException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (HDSSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        synchronized (NotaryService.readyCounter) {
            lastReadyCounter = NotaryService.readyCounter[notaryId][clientId];
        }

        if (request.getReadyClock() <= lastReadyCounter) {
            System.out.println("Replay attack of ready message!");
            throw new RemoteException("Replay attack of ready message!");
        }

        synchronized (NotaryService.readyCounter) {
            NotaryService.readyCounter[notaryId][clientId] = new Integer(request.getReadyClock());
        }


        // if this request exists on echoHashMap
        ClientEcho clientEcho = null;

        String echoIdentifier = "";
        if(request.getType()==Interaction.Type.INTENTION2SELL) {
            echoIdentifier = String.valueOf(request.getUserID()) + String.valueOf(request.getUserClock());
            synchronized (clientEchosMap) {
                if (clientEchosMap.containsKey(echoIdentifier)) {
                    clientEcho = clientEchosMap.get(echoIdentifier);
                } else {
                    clientEcho = new ClientEcho();
                    clientEchosMap.put(echoIdentifier, clientEcho);
                }
            }
        } else {
            System.out.println("ECHO ASNEIRA!!!!!!!!!!!!!!!!!!!!");
        }
        


        VirtualCertificate notaryCert = new VirtualCertificate();
        try {
            notaryCert.init(new File("../HDSNotaryLib/src/main/resources/certs/notary" + request.getNotaryID() + ".crt")
                    .getAbsolutePath());

            try {
                if (Digest.verify(request.getReadySignature(), request.readyString(), notaryCert) == false) {
                    throw new HDSSecurityException("tampering detected in ready message!");
                }
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (HDSSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        clientEcho.addReady(request);
        System.out.println("FILIPE: READY recebi este request " + request.toString() + " do notario "
                + request.getNotaryID() + " e o ID " + echoIdentifier + " e estou com "
                + clientEcho.getNumberOfQuorumReceivedReadys() + " valores iguais no array");

        // ================= Amplification phase! =================
        if ((clientEcho.isSentReady() == false) && (clientEcho.getNumberOfQuorumReceivedReadys() > F)) {
            clientEcho.setSentReady(true);
            ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_NOTARIES);

            CompletionService<Interaction> completionServiceReady = new ExecutorCompletionService<Interaction>(
                    poolExecutor);

            final int idNotary = new Integer(Main.NOTARY_ID);
            request.setNotaryID(idNotary);
            cert = new VirtualCertificate();
            try {
                cert.init("", new File(System.getProperty("project.notary.private")).getAbsolutePath());
                request.setReadySignature(Digest.createDigest(request.readyString(), cert));
            } catch (NoSuchAlgorithmException e1) {
                System.out.println("In amplification: digest of ready message not created! (NoSuchAlgorithm)");
                throw new RemoteException("In amplification: digest of ready message not created! (NoSuchAlgorithm)");
            } catch (HDSSecurityException e) {
                System.out.println("In amplification: digest of ready message not created! (HDSSecurityException) ");
                throw new RemoteException("In amplification: digest of ready message not created! (HDSSecurityException)");
            }

            for (NotaryCommunicationInterface notary : NotaryEchoMiddleware.servers) {
                try {
                    completionServiceReady.submit(new NotaryEchoTask(notary, NotaryEchoTask.Operation.READY, request));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int waited = 0;
            while ((clientEcho.getNumberOfQuorumReceivedReadys() <= (2 * F)) && (!clientEcho.isDelivered())) {
                try {
                    Thread.sleep(500);
                    System.out.println("After Amplification READY sleep " + clientEcho.getNumberOfQuorumReceivedReadys() + " ID " + echoIdentifier);
                    waited++; 
                    if (waited >= 200) {
                        System.out.println("Timeout expired on readys AMPLIFICATION"); 
                        throw new RemoteException("Timeout expired on readys AMPLIFICATION"); 
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(!clientEcho.isDelivered()) {
                if(clientEcho.getDeliveredLock().tryLock()) {
                    clientEcho.setDelivered(true);
                    request = clientEcho.getQuorumReadys();
                    request.setNotaryID(idNotary);
                    //request.setType(Interaction.Type.INTENTION2SELL);

                    // only after receiving readys
                    synchronized (clientEcho) {
                        clientEcho.setDelivered(true);
                        System.out.println("AMPLIFICATION ANTE DE ENTREGAR: " + request.toString());
                        try {
                            if(request.getType()==Interaction.Type.INTENTION2SELL) {
                                NotaryService.getInstance().intentionToSell(request);
                            }
                            if(request.getType()==Interaction.Type.TRANSFERGOOD) {
                                try {
                                    NotaryService.getInstance().transferGood(request);
                                } catch (TransactionException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (GoodException | HDSSecurityException e) {
                            System.out.println(e.getMessage());
                        }
                        finally {
                            clientEcho.getDeliveredLock().unlock();
                        }
                    }
                }
            }
        }
        System.out.println("Varejeira: sai do ready do " + request.getNotaryID() + "**saida**");

    }
}
