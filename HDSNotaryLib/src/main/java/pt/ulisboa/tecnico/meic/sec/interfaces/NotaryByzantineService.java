package pt.ulisboa.tecnico.meic.sec.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotaryByzantineService extends Remote {

    //void broadcastWriteTransfer(int ownerID, int buyerID) throws RemoteException;

    /**
     *
     * @param ownerID
     * @param buyerID
     * @return true if all goes ok
     * @throws RemoteException
     */
    boolean receiveWriteTransfer(int ownerID, int buyerID) throws RemoteException;

    //void broadcastWriteIntention(boolean state, int goodID) throws RemoteException;

    /**
     *
     * @param state
     * @param goodID
     * @return true if all goes ok
     * @throws RemoteException
     */
    boolean receiveWriteIntention(boolean state, int goodID) throws RemoteException;

    //void broadcastReadGetState(int goodID) throws RemoteException;

    /**
     *
     * @param goodID
     * @throws RemoteException
     * @return the state of good
     */
    boolean receiveReadGetState(int goodID) throws RemoteException;




}
