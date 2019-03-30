package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.gui.MenuUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private static ClientService clientInterface;

    public static void main(String[] args){
        int option;
        NotaryInterface notaryInterface;

        try {
            clientInterface = ClientService.getInstance();

            //maven args for client ID, which by default is 1
            if (args.length > 0) {
                ClientService.userID = Integer.parseInt(args[0]);
                ClientService.CLIENT_SERVICE_PORT = 10000 + ClientService.userID;
                ClientService.CLIENT_SERVICE_NAME = "Client" + ClientService.userID;

                if (args.length > 1)
                    ClientService.NOTARY_URI = args[1];
            }

            notaryInterface = ClientService.notaryInterface;

            Registry reg = LocateRegistry.createRegistry(ClientService.CLIENT_SERVICE_PORT);
            reg.rebind(ClientService.CLIENT_SERVICE_NAME, clientInterface);

            System.out.println("Client worker ready");
            System.out.println("Awaiting connections");

            System.out.println(" ====================== DEBUG ============================= ");
            System.out.println(" ClientID           : " + ClientService.userID);
            System.out.println(" Client Service Name: " + ClientService.CLIENT_SERVICE_NAME);
            System.out.println(" Client Service Port: " + ClientService.CLIENT_SERVICE_PORT);
            System.out.println(" Notary URL         : " + ClientService.NOTARY_URI);
            System.out.println(" ====================== DEBUG ============================= ");
            System.out.println("Press any key to dismiss ...");

            try {
                System.in.read();
            } catch (IOException e) {
                System.err.println("** Client: Problem in System.read: " + e.getMessage());
            }

        } catch (RemoteException e) {
            System.err.println("Cannot createDigest ClientServer singleton");
            return;
        }

        try {
            notaryInterface = (NotaryInterface) Naming.lookup(ClientService.NOTARY_URI);
        } catch (NotBoundException e) {
            new BoxUI(":( NotBound on Notary!").show(BoxUI.RED_BOLD_BRIGHT);

        } catch (MalformedURLException e) {
            new BoxUI(":( Malform URL! Cannot find Notary Service!").show(BoxUI.RED_BOLD_BRIGHT);
        } catch (RemoteException e) {
            new BoxUI(":( It looks like I miss the connection with Notary!").show(BoxUI.RED_BOLD_BRIGHT);
        }

        do {
            MenuUI menu = new MenuUI("User client");

            menu.addEntry("To Notary: Intention to sell");
            menu.addEntry("To Notary: Get State of Good");
            menu.addEntry("To User  : Buy Good");
            menu.addEntry("DEBUG -> System state");
            menu.addEntry("Exit");

            option = menu.display();

            if (option == 5) break; //Exit case

            Operation operation = parseOperation(option, clientInterface, notaryInterface);

            if (operation.getAndCheckArgs() == false) {
                new BoxUI("Wrong parameters! Try again!").show(BoxUI.RED_BOLD_BRIGHT);
            } else {
                operation.execute();

                ClientVisitor visitor = new ClientBoxStats();


                if (visitor.check4Failures(operation) == false) {
                    //Display the results using BoxUI when no FAILURES were detected!
                    operation.visit(visitor);
                }


            }


        }while (option != 5);

    }

    public static Operation parseOperation(int option, ClientInterface ci, NotaryInterface ni) {
        switch (option) {
            case 1: return new IntentionToSell(ci, ni);
            case 2: return new GetStateOfGood(ci, ni);
            case 3: return new BuyGood(ci, ni);
            case 4: return new Debug(ci, ni);
        }
        return null;
    }
}
