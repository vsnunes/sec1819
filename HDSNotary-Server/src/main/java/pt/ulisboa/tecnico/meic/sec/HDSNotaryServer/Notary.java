package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
        int registryPort = 8000;
        try {
            Notary n = new Notary();
            System.out.println("After create");
            Registry reg = LocateRegistry.createRegistry(registryPort);
            reg.rebind("HDSNotary", n);

            System.out.println("Notary server ready");
            System.out.println("Awaiting connections");
            System.out.println("Press enter to shutdown");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}

