package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.exceptions.NotaryEchoMiddlewareException;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.util.CCSmartCard;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.cert.Certificate;

public class Main {

    private static final long serialVersionUID = 1L;

    /** Port for accepting clients connection to the service **/
    public static int NOTARY_SERVICE_PORT = 10000;
    public static final String NOTARY_SERVICE_NAME = "HDSNotary";
    public static final String NOTARY_COM_SERVICE_NAME = "HDSNotaryCOM";

    public static int NOTARY_ID = 1;

    /**
     * User's certificates folder location. BE AWARE it must end with slash (/) !
     **/
    public static final String USERS_CERTS_FOLDER = "../HDSNotaryLib/src/main/resources/certs/";

    public static void main(String[] args) throws RemoteException, GoodException, HDSSecurityException {
        
        CCSmartCard card = null;

        if (args.length > 0) {
            NOTARY_ID = Integer.parseInt(args[0]);
            NOTARY_SERVICE_PORT = 10000 + NOTARY_ID;
            System.setProperty("project.notary.private",
                    "../HDSNotaryLib/src/main/resources/certs/java_certs/private_notary" + NOTARY_ID + "_pkcs8.pem");
            System.setProperty("project.notary.cert.path",
                    "../HDSNotaryLib/src/main/resources/certs/notary" + NOTARY_ID + ".crt");
        }
        new BoxUI("Notary is running on port " + NOTARY_SERVICE_PORT).showAndGo(BoxUI.WHITE_BOLD_BRIGHT);

        Registry reg = LocateRegistry.createRegistry(NOTARY_SERVICE_PORT);

        NotaryEchoMiddleware service = null;
        NotaryCommunicationService communicationService = null;

        try {

            communicationService = new NotaryCommunicationService();
            reg.rebind(NOTARY_COM_SERVICE_NAME, communicationService);

            String myURL = System.getProperty("project.notary.rmi") + ":" + NOTARY_SERVICE_PORT + "/" + NOTARY_COM_SERVICE_NAME;
            service = new NotaryEchoMiddleware(System.getProperty("project.nameserver.config"), myURL, NotaryService.getInstance());
            reg.rebind(NOTARY_SERVICE_NAME, service);

        } catch (NotaryEchoMiddlewareException | IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            System.exit(1);
        }

        if (args.length > 1) {
            if (args[1].equals("CCSmartCard"))
                NotaryService.getInstance().setUsingVirtualCerts(false);
        }

        if (NotaryService.getInstance().isUsingVirtualCerts())
            new BoxUI("Notary is using VIRTUAL CERTS!").showAndGo(BoxUI.WHITE_BOLD_BRIGHT);
        else {

            BoxUI box = new BoxUI("Notary is using Cartao do Cidadao!");
            box.initWait("Writing CC Certificate to file ...");

            box.displayWait();

            card = new CCSmartCard();

            try {
                card.init();
                card.writeCitizenAuthCertToFile(USERS_CERTS_FOLDER + "rootcaCC.crt");
                card.stop();
                box.stopWait();
                box.showAndGo(BoxUI.WHITE_BOLD_BRIGHT);
            } catch (HDSSecurityException e) {
                card.stop();
                new BoxUI(e.getMessage()).showAndGo(BoxUI.RED_BOLD_BRIGHT);
                System.exit(1);
            }

        }

        

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

        //if using CC smart card release the SDK!
        if (!NotaryService.getInstance().isUsingVirtualCerts()) {
            card.stop();
        }

        System.exit(0);


    }
}

