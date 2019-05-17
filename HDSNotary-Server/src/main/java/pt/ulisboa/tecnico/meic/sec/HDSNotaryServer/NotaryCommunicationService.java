package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.exceptions.NotaryEchoMiddlewareException;
import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.interfaces.NotaryCommunicationInterface;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.util.*;
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
        System.out.println("Received echo from notary " + request.getNotaryID());
        int clientId = -1;
        if(request.getType()==Type.INTENTION2SELL) {
            clientId = request.getUserID();
        }
        else if (request.getType()==Type.TRANSFERGOOD) {
            clientId = request.getSellerID();
        } 
        else {
            System.out.println("operation not found!");
        }
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
            if(request.getType()==Type.INTENTION2SELL) {
                if (!Digest.verify(request, cert)) {
                    throw new RemoteException("You are not user " + clientId + "!!");
                }
            } else if (request.getType()==Type.TRANSFERGOOD) {
                String data = "" + request.getSellerID() + request.getBuyerID() + request.getGoodID() + request.getSellerClock() + request.getBuyerClock();
                if(!Digest.verify(request.getSellerHMAC(), data, cert)){
                    throw new RemoteException("You are not user " + clientId + "!!");
                }
            } 
            else {
                System.out.println("Operation not find");
                throw new RemoteException("Operation not find");
            }
        } catch (NoSuchAlgorithmException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (HDSSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("You are not the correct user!\n"+e.getMessage());
            throw new RemoteException("You are not the correct user!");
        }

        synchronized (NotaryService.echoCounter) {
            lastEchoCounter = NotaryService.echoCounter[notaryId][clientId];
        }

        if (request.getEchoClock() <= lastEchoCounter) {
            System.out.println("Replay attack of echo message! From Notary " + notaryId);
            throw new RemoteException("Replay attack of echo message! From Notary " + notaryId);
        }

        synchronized (NotaryService.echoCounter) {
            NotaryService.echoCounter[notaryId][clientId] = new Integer(request.getEchoClock());
            NotaryService.doWriteRB();
        }

        // if this request exists on echoHashMap
        ClientEcho clientEcho = null;

        String echoIdentifier = "";
        if(request.getType()==Interaction.Type.INTENTION2SELL) {
            echoIdentifier = "ITS" + String.valueOf(request.getUserID()) + String.valueOf(request.getUserClock());
            synchronized (clientEchosMap) {
                if (clientEchosMap.containsKey(echoIdentifier)) {
                    clientEcho = clientEchosMap.get(echoIdentifier);
                } else {
                    clientEcho = new ClientEcho();
                    clientEchosMap.put(echoIdentifier, clientEcho);
                }
            }
        } else {
            echoIdentifier = "TG" + String.valueOf(request.getBuyerID()) + String.valueOf(request.getBuyerClock()) + String.valueOf(request.getSellerID()) + String.valueOf(request.getSellerClock());
            synchronized (clientEchosMap) {
                if (clientEchosMap.containsKey(echoIdentifier)) {
                    clientEcho = clientEchosMap.get(echoIdentifier);
                } else {
                    clientEcho = new ClientEcho();
                    clientEchosMap.put(echoIdentifier, clientEcho);
                }
            }
        }


        try {
            Certification notaryCert = getCert();

            // compare hmacs
            try {
                if (Digest.verify(request.getNotaryIDSignature(), request.echoString(), notaryCert) == false) {
                    throw new HDSSecurityException("tampering detected in echo message!");
                }
            } catch (NoSuchAlgorithmException | HDSSecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            clientEcho.addEcho(request);

        } catch (HDSSecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


    }

    @Override
    public void ready(Interaction request) throws RemoteException {
        System.out.println("Received ready from notary " + request.getNotaryID());
        int clientId = -1;
        if(request.getType()==Type.INTENTION2SELL) {
            clientId = request.getUserID();
        }
        else if (request.getType()==Type.TRANSFERGOOD) {
            clientId = request.getSellerID();
        } 

        else {
            System.out.println("Operation not found!");
        }
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
            if(request.getType()==Type.INTENTION2SELL) {
                if (!Digest.verify(request, cert)) {
                    throw new RemoteException("You are not user " + clientId + "!!");
                }
            } else if (request.getType()==Type.TRANSFERGOOD) {
                String data = "" + request.getSellerID() + request.getBuyerID() + request.getGoodID() + request.getSellerClock() + request.getBuyerClock();
                if(!Digest.verify(request.getSellerHMAC(), data, cert)){
                    throw new RemoteException("You are not user " + clientId + "!!");
                }
            } 
            else {
                System.out.println("Operation not find");
                throw new RemoteException("Operation not find");
            }
        } catch (NoSuchAlgorithmException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (HDSSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("You are not the correct user!\n" + e.getMessage());
            throw new RemoteException("You are not the correct user!");
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
            echoIdentifier = "ITS" + String.valueOf(request.getUserID()) + String.valueOf(request.getUserClock());
            synchronized (clientEchosMap) {
                if (clientEchosMap.containsKey(echoIdentifier)) {
                    clientEcho = clientEchosMap.get(echoIdentifier);
                } else {
                    clientEcho = new ClientEcho();
                    clientEchosMap.put(echoIdentifier, clientEcho);
                }
            }
        } else {
            echoIdentifier = "TG" + String.valueOf(request.getBuyerID()) + String.valueOf(request.getBuyerClock()) + String.valueOf(request.getSellerID()) + String.valueOf(request.getSellerClock());
            synchronized (clientEchosMap) {
                if (clientEchosMap.containsKey(echoIdentifier)) {
                    clientEcho = clientEchosMap.get(echoIdentifier);
                } else {
                    clientEcho = new ClientEcho();
                    clientEchosMap.put(echoIdentifier, clientEcho);
                }
            }
        }
        



        try {
            Certification notaryCert = getCert();

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

        // ================= Amplification phase! =================
        if ((clientEcho.isSentReady() == false) && (clientEcho.getNumberOfQuorumReceivedReadys() > F)) {
            ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_NOTARIES);

            CompletionService<Interaction> completionServiceReady = new ExecutorCompletionService<Interaction>(
                    poolExecutor);

            final int idNotary = new Integer(Main.NOTARY_ID);
            request.setNotaryID(idNotary);

            try {
                cert = getCert();
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
                    waited++;
                    if (waited >= 200) {
                        System.out.println("Timeout expired on readys AMPLIFICATION"); 
                        throw new RemoteException("Timeout expired on readys AMPLIFICATION"); 
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            clientEcho.setSentReady(true);


            if(!clientEcho.isDelivered()) {
                if(clientEcho.getDeliveredLock().tryLock()) {
                    System.out.println("AMPLIFICATION PHASE: acquired the lock, the request will be handled here. So" +
                            " no response to the client");
                    clientEcho.setDelivered(true);
                    request = clientEcho.getQuorumReadys();
                    request.setNotaryID(idNotary);

                    // only after receiving readys
                    synchronized (clientEcho) {
                        clientEcho.setDelivered(true);
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
    }

    private Certification getCert() throws HDSSecurityException {
        Certification cert = null;
        NotaryService notaryService = null;
        try {
            notaryService = NotaryService.getInstance();
            //VIRTUAL CERTS
            if (notaryService.isUsingVirtualCerts()) {
                cert = new VirtualCertificate();
                cert.init("", new File(System.getProperty("project.notary.private")).getAbsolutePath());
            } else {
                cert = new CCSmartCard();
                cert.init();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (GoodException e) {
            e.printStackTrace();
        }


        return cert;
    }
}
