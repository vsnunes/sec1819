package pt.ulisboa.tecnico.meic.sec.HDSNotaryBadClient;

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
                ClientService.CLIENT_SERVICE_PORT = 9999;
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
            System.out.println(" MALICIOUS CLIENT!");
            System.out.println(" ====================== DEBUG ============================= ");
            System.out.println("Press any key to dismiss ...");

            try {
                System.in.read();
            } catch (IOException e) {
                System.err.println("** Client: Problem in System.read: " + e.getMessage());
            }

        } catch (RemoteException e) {
            System.err.println("Cannot create Digest ClientServer singleton " + e.getMessage());
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

            menu.addEntry("Tampering: Intention to sell");
            menu.addEntry("Tampering Response: Intention to sell");
            menu.addEntry("Replay Attack: Intention to sell");
            menu.addEntry("Replay Attack Response: Intention to sell");
            menu.addEntry("Altered Key: Intention to sell");
            menu.addEntry("Tampering: Get State of Good");
            menu.addEntry("Tampering Response: Get State of Good");
            menu.addEntry("Replay Attack: Get State of Good");
            menu.addEntry("Replay Attack Response: Get State of Good");
            menu.addEntry("Tampering: Buy Good");
            menu.addEntry("Tampering Response: Buy Good");
            menu.addEntry("Replay Attack: Buy Good");
            menu.addEntry("Replay Attack Response: Buy Good");
            menu.addEntry("DEBUG -> System state");
            menu.addEntry("Exit");

            option = menu.display();

            if (option == 15) break; //Exit case

            Operation operation = parseOperation(option, clientInterface, notaryInterface);

            if (!operation.getAndCheckArgs()) {
                new BoxUI("Wrong parameters! Try again!").show(BoxUI.RED_BOLD_BRIGHT);
            } else {

                /** Animation Thread **/
                Thread waitingThread = new Thread() {
                    public void run() {
                        int i = 0;
                        //Animation when waiting for responses
                        char[] animationChars = new char[] {'|', '/', '-', '\\'};
                        String a = "";

                        System.out.print("  Processing ...");
                        System.out.flush();
                        while(true) {

                            try {
                                Thread.sleep(100);

                                a = "\r" + animationChars[i++ % 4];
                                try {
                                    System.out.write(a.getBytes());
                                } catch (IOException e) {
                                    System.out.println("IO Error");
                                }
                                System.out.flush();
                            } catch (InterruptedException e) {
                                System.out.println("Done!            ");
                                System.out.flush();
                                return;
                            }
                        }
                    }};


                //Display waiting animation :)
                waitingThread.start();

                operation.execute();

                //Stopping waiting animation
                waitingThread.interrupt();


                ClientVisitor visitor = new ClientBoxStats();


                if (!visitor.check4Failures(operation)) {
                    //Display the results using BoxUI when no FAILURES were detected!
                    operation.visit(visitor);
                }


            }


        }while (true);

    }

    public static Operation parseOperation(int option, ClientInterface ci, NotaryInterface ni) {
        switch (option) {
            case 1: return new IntentionToSellTampered(ci, ni);
            case 2: return new IntentionToSellTamperedNotary(ci, ni);
            case 3: return new IntentionToSellReplay(ci, ni);
            case 4: return new IntentionToSellReplayNotary(ci, ni);
            case 5: return new IntentionToSellAlteredKey(ci, ni);
            case 6: return new GetStateOfGoodTampering(ci, ni);
            case 7: return new GetStateOfGoodTamperingNotary(ci, ni);
            case 8: return new GetStateOfGoodReplay(ci,ni);
            case 9: return new GetStateOfGoodReplayNotary(ci, ni);
            case 10: return new BuyGoodTampered(ci, ni);
            case 11: return new BuyGoodTamperedNotary(ci, ni);
            case 12: return new BuyGoodReplay(ci, ni);
            case 13: return new BuyGoodReplayNotary(ci, ni);
            case 14: return new Debug(ci, ni);
        }
        return null;
    }
}