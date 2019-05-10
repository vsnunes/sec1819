package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.interfaces.NotaryCommunicationInterface;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.util.CFGHelper;
import pt.ulisboa.tecnico.meic.sec.util.Digest;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;
import pt.ulisboa.tecnico.meic.sec.util.VirtualCertificate;

import java.io.File;
import java.io.IOException;
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

public class NotaryCommunicationService implements NotaryCommunicationInterface {

    @Override
    public void echo(Interaction request) throws RemoteException {
        int clientId = request.getClientID();
        int notaryId = request.getNotaryID();
        int lastEchoCounter = NotaryService.echoCounter[notaryId];
        
        if (request.getEchoClock() <= lastEchoCounter) {
            throw new RemoteException("Replay attack of echo message!");
        }

        ClientEcho clientEcho = NotaryEchoMiddleware.clientEchos.get(clientId);

        VirtualCertificate notaryCert = new VirtualCertificate();
        try {
            notaryCert.init(new File(System.getProperty("project.notary.cert.path")).getAbsolutePath());
        } catch (HDSSecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        /* compare hmacs */
        try {
            if (Digest.verify(request.getNotaryIDSignature(), request.echoString(), notaryCert) == false) {
                throw new HDSSecurityException("tampering detected in echo message!");
            }
        } catch (NoSuchAlgorithmException | HDSSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
 
        synchronized (clientEcho.getEchos()) {
            Interaction notaryInteraction = clientEcho.getEchos()[notaryId];
            if (notaryInteraction == null) {
                clientEcho.addEcho(notaryId, request);
                clientEcho.getQuorumEchos().signal();
            }
        }
    }

    @Override
    public void ready(Interaction request) throws RemoteException {
        int clientId = request.getClientID();
        int notaryId = request.getNotaryID();
        int lastReadyCounter = NotaryService.readyCounter[notaryId];

        if (request.getReadyClock() <= lastReadyCounter) {
            throw new RemoteException("Replay attack of ready message!");
        }

        ClientEcho clientEcho = NotaryEchoMiddleware.clientEchos.get(clientId);

        VirtualCertificate notaryCert = new VirtualCertificate();
        try {
            notaryCert.init(new File(System.getProperty("project.notary.cert.path")).getAbsolutePath());

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
                clientEcho.getQuorumReadys().signal();
            }
        }
    }
}
