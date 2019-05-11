package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.interfaces.NotaryCommunicationInterface;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.util.CFGHelper;
import pt.ulisboa.tecnico.meic.sec.util.Digest;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;
import pt.ulisboa.tecnico.meic.sec.util.VirtualCertificate;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class NotaryCommunicationService implements NotaryCommunicationInterface, Serializable {

    @Override
    public void echo(Interaction request) throws RemoteException {
        
        System.out.println("Varejeira: recebi echo do " + request.getNotaryID());
        /*int clientId = request.getUserID();
        int notaryId = request.getNotaryID();
        int lastEchoCounter = -1;
        synchronized(NotaryService.echoCounter) {
            lastEchoCounter = NotaryService.echoCounter[notaryId];
        }

        System.out.println("varejeira lastEchoCounter " + lastEchoCounter);
        System.out.println("varejeira echoClock " + request.getEchoClock());
        if (request.getEchoClock() <= lastEchoCounter) {
            throw new RemoteException("Replay attack of echo message!");
        }

        synchronized(NotaryService.echoCounter) {
            NotaryService.echoCounter[notaryId] = new Integer(request.getEchoClock());
        }

        System.out.println("Varejeira after checking echo clock");
        ClientEcho clientEcho = null;
        synchronized(NotaryService.echoCounter[clientId]) {
            clientEcho = NotaryEchoMiddleware.clientEchos[clientId];
        }

        VirtualCertificate notaryCert = new VirtualCertificate();
        try {
            notaryCert.init(new File("../HDSNotaryLib/src/main/resources/certs/notary" + request.getNotaryID() + ".crt").getAbsolutePath());
        } catch (HDSSecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        System.out.println("Varejeira after cert");
        //compare hmacs 
        try {
            if (Digest.verify(request.getNotaryIDSignature(), request.echoString(), notaryCert) == false) {
                throw new HDSSecurityException("tampering detected in echo message!");
            }
        } catch (NoSuchAlgorithmException | HDSSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Varejeira after sign");
        synchronized (clientEcho.getEchos()) {
            Interaction notaryInteraction = clientEcho.getEchos()[notaryId];
            if (notaryInteraction == null) {
                System.out.println("Varejeira after if notaryInteraction a null");
                clientEcho.addEcho(notaryId, request);
                System.out.println("Varejeira after addEcho");
                try {
                    clientEcho.getLock().lock();
                    clientEcho.getQuorumEchos().signal();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Varejeira after signal");
            }
        }
        System.out.println("Varejeira leaving echo function");*/
    }

    @Override
    public void ready(Interaction request) throws RemoteException {
        System.out.println("Varejeira: recebi echo!");
        int clientId = request.getUserID();
        int notaryId = request.getNotaryID();
        int lastReadyCounter = -1;
        synchronized(NotaryService.readyCounter) {
            lastReadyCounter = NotaryService.readyCounter[notaryId];
        }

        if (request.getReadyClock() <= lastReadyCounter) {
            throw new RemoteException("Replay attack of ready message!");
        }

        synchronized(NotaryService.readyCounter) {
            NotaryService.readyCounter[notaryId] = new Integer(request.getReadyClock());
        }

        ClientEcho clientEcho = null;
        synchronized(NotaryService.echoCounter[clientId]) {
            clientEcho = NotaryEchoMiddleware.clientEchos[clientId];
        }

        VirtualCertificate notaryCert = new VirtualCertificate();
        try {
            notaryCert.init(new File("../HDSNotaryLib/src/main/resources/certs/notary" + request.getNotaryID() + ".crt").getAbsolutePath());

            try {
                if (Digest.verify(request.getNotaryIDSignature(), request.echoString(), notaryCert) == false) {
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

        synchronized (clientEcho.getReadys()) {
            Interaction notaryInteraction = clientEcho.getReadys()[notaryId];
            if (notaryInteraction == null) {
                clientEcho.addReady(notaryId, request);
                clientEcho.getLock().lock();
                clientEcho.getQuorumReadys().signal();
            }
        }
    }
}
