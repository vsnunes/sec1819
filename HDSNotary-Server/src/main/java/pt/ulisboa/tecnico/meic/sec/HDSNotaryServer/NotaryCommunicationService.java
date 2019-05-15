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
        /*ClientEcho clientEcho = null;
        synchronized (NotaryEchoMiddleware.clientEchos) {
            clientEcho = NotaryEchoMiddleware.clientEchos[clientId];
        }*/

        
        //if this request exists on echoHashMap
        ClientEcho clientEcho = null;

        String echoIdentifier = String.valueOf(request.getUserID()) + String.valueOf(request.getUserClock());
        if(clientEchosMap.containsKey(echoIdentifier)) {
            clientEcho = clientEchosMap.get(echoIdentifier);
        } else {
            clientEcho = new ClientEcho();
            clientEchosMap.put(echoIdentifier, clientEcho);
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
        
        Interaction notaryInteraction = clientEcho.getEchos()[notaryId];
        //if (notaryInteraction == null) {
            // System.out.println("Varejeira after if notaryInteraction a null");
            System.out.println("FILIPE: ECHO recebi este request " + request.toString() + " do notario " + request.getNotaryID()
                                 + " e eu sou o " + Main.NOTARY_ID);
            clientEcho.addEcho(notaryId, request);
                
            // System.out.println("Varejeira after addEcho");
            /*ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
            CompletionService<Interaction> completionService = new ExecutorCompletionService<Interaction>(poolExecutor);

            NotaryCommunicationInterface server = NotaryEchoMiddleware.servers.get(notaryId - 1);
            completionService.submit(new NotaryEchoTask(server, NotaryEchoTask.Operation.SIGNALECHO, clientId));*/
        //}
        
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

        /*ClientEcho clientEcho = null;
        synchronized (NotaryEchoMiddleware.clientEchos[clientId]) {
            clientEcho = NotaryEchoMiddleware.clientEchos[clientId];
            NotaryService.doWriteRB();
        }*/

        //if this request exists on echoHashMap
        ClientEcho clientEcho = null;

        String echoIdentifier = String.valueOf(request.getUserID()) + String.valueOf(request.getUserClock());
        if(clientEchosMap.containsKey(echoIdentifier)) {
            clientEcho = clientEchosMap.get(echoIdentifier);
        } else {
            clientEcho = new ClientEcho();
            clientEchosMap.put(echoIdentifier, clientEcho);
        }

        /*try {
            NotaryService.getInstance().debugPrintBCArrays();
        } catch (GoodException e2) {
            e2.printStackTrace();
        }*/

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

        
        Interaction notaryInteraction = clientEcho.getReadys()[notaryId];
        System.out.println("FILIPE: READY recebi este request " + request.toString() + " do notario " + request.getNotaryID()
                                 + " e eu sou o " + Main.NOTARY_ID);
            clientEcho.addReady(notaryId, request);

            /*ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
            CompletionService<Interaction> completionService = new ExecutorCompletionService<Interaction>(poolExecutor);

            NotaryCommunicationInterface server = NotaryEchoMiddleware.servers.get(notaryId - 1);
            completionService.submit(new NotaryEchoTask(server, NotaryEchoTask.Operation.SIGNALREADY, clientId));*/
    

        // ================= Amplification phase! =================

        /*if ((clientEcho.isSentReady() == false) && (clientEcho.getNumberOfQuorumReceivedReadys() > F)) {
            System.out.println("VAREJEIRA ENTREI NA FASE DE AMPLIFICAÇÃO!!!!!!");
            clientEcho.setSentReady(true);
            boolean receivedAllReadys = false;
            NotaryService notaryService;
            try {
                notaryService = NotaryService.getInstance();
                List<NotaryCommunicationInterface> servers;
                try {
                    servers = initRMI();


                    request = clientEcho.getQuorum();
                    final int idNotary = new Integer(Main.NOTARY_ID);
                    request.setNotaryID(idNotary);
                    int readyClock = NotaryService.readyCounter[idNotary] + 1;
                    request.setReadyClock(readyClock);
                    
                    Certification cert = new VirtualCertificate();
                    
                    try {
                        cert.init("", new File(System.getProperty("project.notary.private")).getAbsolutePath());
                        request.setReadySignature(Digest.createDigest(request.readyString(), cert));
                    } catch (NoSuchAlgorithmException e1) {
                        e1.printStackTrace();
                    } catch (HDSSecurityException e) {
                        e.printStackTrace();
                    }


                    CompletionService<Interaction> completionService = new ExecutorCompletionService<Interaction>(
                            poolExecutor);

                    for (NotaryCommunicationInterface notary : servers) {
                        try {
                            completionService.submit(
                                    new NotaryEchoTask(notary, NotaryEchoTask.Operation.READY, request));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("varejeira after amplification");

                    clientEcho.getLock().lock();
                    try {
                        while (clientEcho.getNumberOfQuorumReceivedReadys() < (2 * F)
                                && clientEcho.isDelivered() == true) {
                            try {
                                receivedAllReadys = clientEcho.getQuorumReadys().await(TIMEOUT_SEC, TimeUnit.SECONDS);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    } finally {
                        clientEcho.getLock().unlock();
                    }
    
                    if (clientEcho.getNumberOfQuorumReceivedReadys() > (2 * F)) {
                        receivedAllReadys = true;
                    }

                    //readys timeout expired!
                    if (receivedAllReadys == false) {
                        throw new RemoteException("Failed during ready propagation phase on amplification :(");
                    }
                    if (clientEcho.isDelivered() == false) {
                        //only after receiving readys
                        synchronized(clientEcho) {
                            
                            try {
                                Interaction quorumRequest = clientEcho.getQuorum();
                                if (quorumRequest.getType() == Interaction.Type.INTENTION2SELL) {
                                    notaryService.intentionToSell(quorumRequest);
                                    clientEcho.setDelivered(true);
                                    
                                } else if (quorumRequest.getType() == Interaction.Type.TRANSFERGOOD) {
                                    notaryService.transferGood(quorumRequest);
                                    clientEcho.setDelivered(true);
                                } else {
                                    System.out.println("Ready: No suitable operation found! Type is null? " + (quorumRequest.getType() == null));
                                }
                            } catch (GoodException e) {
                                e.printStackTrace();
                            } catch (HDSSecurityException e) {
                                e.printStackTrace();
                            } catch (TransactionException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (NotaryEchoMiddlewareException e1) {
                    e1.printStackTrace();
                }
            } catch (GoodException e1) {
                e1.printStackTrace();
            }
        }*/

    }
}
