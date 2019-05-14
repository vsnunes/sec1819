package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.interfaces;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.Good;

import java.rmi.Remote;
import java.rmi.RemoteException;
import pt.ulisboa.tecnico.meic.sec.util.*;
/**
 * An interface for Authenticated Echo Broadcast
 */
public interface NotaryCommunicationInterface extends Remote {

    /**
     * Sends a echo
     */
    void echo(Interaction request) throws RemoteException;

    void ready(Interaction request) throws RemoteException;

    void signalEcho(int clientId) throws RemoteException;

    void signalReady(int clientId) throws RemoteException;


}
