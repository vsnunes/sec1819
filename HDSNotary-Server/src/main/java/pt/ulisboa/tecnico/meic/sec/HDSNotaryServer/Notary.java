package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import javafx.util.Pair;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Notary extends UnicastRemoteObject implements NotaryInterface{

    private static final long serialVersionUID = 1L;

    private Notary() throws RemoteException {
        super();
    }

    @Override
    public boolean intentionToSell(int userId, int goodId) throws RemoteException{
        return false;
    }


    public static void main(String[] args){
        try{
            Naming.rebind("rmi://localhost:8585/HDSNotary", new Notary());
            System.err.println("Notary is up");
        }catch(RemoteException e){
            System.err.println("Notary could not start: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }catch(MalformedURLException e) {
            System.err.println("URL is not formed correctly");
            System.exit(-1);
        }
    }
}
