package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {

    private static final long serialVersionUID = 1L;

    /** Port for accepting clients connection to the service **/
    private static final int NOTARY_SERVICE_PORT = 10000;
    private static final String NOTARY_SERVICE_NAME = "HDSNotary";



    public static void main(String[] args) throws RemoteException, GoodException{

        //try {
            NotaryService service = new NotaryService();
            //service.createUser();
            //service.createGood();

            Registry reg = LocateRegistry.createRegistry(NOTARY_SERVICE_PORT);
            reg.rebind(NOTARY_SERVICE_NAME, service);

            System.out.println("Main server ready");
            System.out.println("Awaiting connections");
            System.out.println("Press enter to shutdown");

            //Wait for connections
            try {
                System.in.read();
            } catch (IOException e) {
                System.err.println("** NOTARY: Problem in System.read: " + e.getMessage());
                e.printStackTrace();
            }


            System.exit(0);

        /*} catch (RemoteException e) {
            System.err.println("** NOTARY: Problem binding server: " + e.getMessage());
            e.printStackTrace();
        } catch (GoodException e){
            System.err.println("**NOTARY: Problem with good ID");
        }*/

    }
}

