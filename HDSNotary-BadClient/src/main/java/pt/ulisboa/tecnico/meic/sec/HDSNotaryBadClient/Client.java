package pt.ulisboa.tecnico.meic.sec.HDSNotaryBadClient;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryBadClient.exceptions.NotaryMiddlewareException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.gui.MenuUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private static ClientService clientInterface;

    public static void main(String[] args){
        int option;
        NotaryMiddleware notaryInterface;

        try {


            //maven args for client ID, which by default is 1
            if (args.length > 0) {
                ClientService.userID = Integer.parseInt(args[0]);
                if(ClientService.userID < 1 || ClientService.userID > 5){
                    new BoxUI("user not found").show(BoxUI.RED_BOLD_BRIGHT);
                    return;
                }
                ClientService.CLIENT_SERVICE_PORT = 10010 + ClientService.userID;
                ClientService.CLIENT_SERVICE_NAME = "Client" + ClientService.userID;

                if (args.length > 1) {
                    if (args[1].equals("CCSmartCard")) {
                        ClientService.NOTARY_USES_VIRTUAL = false;

                        //Sets the notary cert path CC Certificate and not the virtual one
                        System.setProperty("project.notary.cert.path", System.getProperty("project.notary.certCC.path"));
                    } else {
                        ClientService.NOTARY_USES_VIRTUAL = true;
                    }
                }
            }
            clientInterface = ClientService.getInstance();
            notaryInterface = ClientService.notaryInterface;

            Registry reg = LocateRegistry.createRegistry(ClientService.CLIENT_SERVICE_PORT);
            reg.rebind(ClientService.CLIENT_SERVICE_NAME, clientInterface);

            System.out.println("Client worker ready");
            System.out.println("Awaiting connections");

            System.out.println(" ====================== DEBUG ============================= ");
            System.out.println(" ClientID           : " + ClientService.userID);
            System.out.println(" Client Service Name: " + ClientService.CLIENT_SERVICE_NAME);
            System.out.println(" Client Service Port: " + ClientService.CLIENT_SERVICE_PORT);
            System.out.println(" Authentication     : " + ((ClientService.NOTARY_USES_VIRTUAL) ? "Virtual Certificates" : "Cartao do Cidadao"));
            System.out.println(" ====================== DEBUG ============================= ");
            System.out.println("Press any key to dismiss ...");

            try {
                System.in.read();
            } catch (IOException e) {
                System.err.println("** Client: Problem in System.read: " + e.getMessage());
            }

        } catch (RemoteException e) {
            System.err.println("** Client: Remoting problem: " + e.getMessage());
            return;
        } catch (NotaryMiddlewareException e) {
            new BoxUI(e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
            return;
        } catch (IOException e) {
            new BoxUI(e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
            return;
        }


        do {
            MenuUI menu = new MenuUI("User client");

            menu.addEntry("To Notary: Intention to sell");
            menu.addEntry("To Notary: Get State of Good");
            menu.addEntry("Buy Good Without Proof of Work");
            menu.addEntry("DEBUG -> System state");
            menu.addEntry("Exit");
            menu.addEntry("Intention to sell broadcast to 2 out of 4 notaries");
            menu.addEntry("Intention to sell broadcast to 3 out of 4 notaries");

            option = menu.display();

            if (option == 5) {
                notaryInterface.shutdown();
                break; //Exit case
            }

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

    public static Operation parseOperation(int option, ClientInterface ci, NotaryMiddleware ni) {
        switch (option) {
            case 1: return new IntentionToSell(ci, ni);
            case 2: return new GetStateOfGood(ci, ni);
            case 3: return new BuyGoodWithoutPoW(ci, ni);
            case 4: return new Debug(ci, ni);
            case 6: return new IntentionToSellTo2(ci,ni);
            case 7: return new IntentionToSellTo3(ci,ni);

        }
        return null;
    }
}
