package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.gui.MenuUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

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
        Thread thread;

        try {
            clientInterface = ClientService.getInstance();

            notaryInterface = ClientService.notaryInterface;


            //In order to the client be able to execute operations and receiving others client requests this
            //needs to be in a new thread
            thread = new Thread(){
                public void run(){
                    try {
                        Registry reg = LocateRegistry.createRegistry(ClientService.CLIENT_SERVICE_PORT);
                        reg.rebind(ClientService.CLIENT_SERVICE_NAME, clientInterface);
                    } catch (RemoteException e) {
                        System.out.println("** CLIENT WORKER THREAD: Cannot register and rebind ClientService!");
                        return;
                    }
                    System.out.println("Client worker ready");
                    System.out.println("Awaiting connections");
                }
            };

            thread.start();



        } catch (RemoteException e) {
            System.err.println("Cannot create ClientServer singleton");
            return;
        }


        do {
        MenuUI menu = new MenuUI("User client");

        menu.addEntry("To Notary: Intention to sell");
        menu.addEntry("To Notary: Get State of Good");
        menu.addEntry("DEBUG -> To Notary: Transfer Good");
        menu.addEntry("To User  : Buy Good");
        menu.addEntry("Exit");
        menu.addEntry("DEBUG -> System state");

        option = menu.display();

        int good, buyer;
        boolean response;
        String clientURL;
        ClientInterface anotherClient;


            try {
                notaryInterface = (NotaryInterface) Naming.lookup(ClientService.NOTARY_URI);
            } catch (NotBoundException e) {
                new BoxUI(":( NotBound on Notary!").show(BoxUI.RED_BOLD_BRIGHT);

            } catch (MalformedURLException e) {
                new BoxUI(":( Malform URL! Cannot find Notary Service!").show(BoxUI.RED_BOLD_BRIGHT);
            } catch (RemoteException e) {
                new BoxUI(":( It looks like I miss the connection with Notary!").show(BoxUI.RED_BOLD_BRIGHT);
            }


            switch (option) {
            // === INTENTION TO SELL ===
            case 1:
                good = Integer.parseInt(new BoxUI("What is the good ID?").showAndGet());
                boolean intention = Boolean.parseBoolean(new BoxUI("To sell?").showAndGet());

                try {

                    response = notaryInterface.intentionToSell(ClientService.userID, good, intention);
                }
                catch(GoodException e) {
                    new BoxUI("Notary report the following problem: " + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
                    break;
                }
                catch (RemoteException e) {
                    new BoxUI("There were a problem in connecting to Notary!").show(BoxUI.RED_BOLD_BRIGHT);
                    break;
                }

                if (response == true) {
                    new BoxUI("The item is now for sale!").show(BoxUI.GREEN_BOLD);
                }
                else {
                    new BoxUI("The item is now NOT for sale!").show(BoxUI.GREEN_BOLD);
                }

                break;

                // === GET STATE OF GOOD ===
            case 2:
                good = Integer.parseInt(new BoxUI("What is the good ID?").showAndGet());

                try {
                    response = notaryInterface.getStateOfGood(good);
                }
                catch(GoodException e) {
                    new BoxUI("Notary report the following problem: " + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
                    break;
                }
                catch (RemoteException e) {
                    new BoxUI("There were a problem in connecting to Notary!").show(BoxUI.RED_BOLD_BRIGHT);
                    break;
                }

                if (response == true) {
                    new BoxUI("The item is for sale!").show(BoxUI.GREEN_BOLD);
                } else new BoxUI("The item is NOT for sale!").show(BoxUI.RED_BOLD);

                break;


                // === TRANSFER GOOD ===
                case 3:
                    good =  Integer.parseInt(new BoxUI("What is the good ID to transfer?").showAndGet());
                    buyer = Integer.parseInt(new BoxUI("What is the buyer ID?").showAndGet());

                    try {
                        response = notaryInterface.transferGood(ClientService.userID, buyer, good);
                    } catch (RemoteException e) {
                        new BoxUI("There were a problem in connecting to Notary!").show(BoxUI.RED_BOLD_BRIGHT);
                        break;

                    } catch (TransactionException e) {
                        new BoxUI("Notary report the following problem: " + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
                        break;
                    }

                    if (response == true) {
                        new BoxUI("Successfully transferred good!").show(BoxUI.GREEN_BOLD);
                    } else new BoxUI("There was an error on the transferring process!").show(BoxUI.RED_BOLD);

                    break;

                case 4:
                    clientURL = new BoxUI("What is the client URL?").showAndGet();
                    good =  Integer.parseInt(new BoxUI("What is the good ID to buy?").showAndGet());

                    try {
                        anotherClient = (ClientInterface) Naming.lookup(clientURL);

                        response = anotherClient.buyGood(good, ClientService.userID);

                        if (response == true) {
                            new BoxUI("Successfully bought good!").show(BoxUI.GREEN_BOLD);
                        } else new BoxUI("Seller didn't sell the good!").show(BoxUI.RED_BOLD);

                    } catch (NotBoundException e) {
                        new BoxUI(":( NotBound on Client!").show(BoxUI.RED_BOLD_BRIGHT);

                    } catch (MalformedURLException e) {
                        new BoxUI(":( Malform URL! Cannot find Client Service!").show(BoxUI.RED_BOLD_BRIGHT);
                    } catch (RemoteException e) {
                        new BoxUI(":( It looks like I miss the connection with Client!").show(BoxUI.RED_BOLD_BRIGHT);
                    }

                    break;

                case 6:
                    try {
                        notaryInterface.doPrint();
                    } catch (RemoteException e) {
                        new BoxUI("There were a problem in connecting to Notary!").show(BoxUI.RED_BOLD_BRIGHT);
                        break;
                    }
                    break;
        }

        }while (option != 5);

        System.out.println("Terminating worker thread...");
        thread.interrupt();
    }
}
