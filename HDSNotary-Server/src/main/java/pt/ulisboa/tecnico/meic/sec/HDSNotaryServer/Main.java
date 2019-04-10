package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {

    private static final long serialVersionUID = 1L;

    /** Port for accepting clients connection to the service **/
    public static final int NOTARY_SERVICE_PORT = 10000;
    public static final String NOTARY_SERVICE_NAME = "HDSNotary";

    /** User's certificates folder location. BE AWARE it must end with slash (/) ! **/
    public static final String USERS_CERTS_FOLDER = "../HDSNotaryLib/src/main/resources/certs/";



    public static void main(String[] args) throws RemoteException, GoodException{

        NotaryService service = NotaryService.getInstance();
        //service.createUser();
        //service.createGood();

        if (args.length > 1) {
            if (args[0].equals("CCSmartCard"))
                service.setUsingVirtualCerts(false);
        }

        if (service.isUsingVirtualCerts())
            new BoxUI("Notary is using VIRTUAL CERTS!").show(BoxUI.WHITE_BOLD_BRIGHT);
        else new BoxUI("Notary is using Cartao do Cidadao!").show(BoxUI.WHITE_BOLD_BRIGHT);

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


    }
}

