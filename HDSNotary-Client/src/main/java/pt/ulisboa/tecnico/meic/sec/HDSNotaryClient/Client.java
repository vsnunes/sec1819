package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.gui.MenuUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {

    private static final String NOTARY_URI = "//localhost:10000/HDSNotary";

    private static NotaryInterface notaryInterface;

    private static int userID = 1;

    public static void main(String[] args){
        int option;

        do {
        MenuUI menu = new MenuUI("User client");

        menu.addEntry("To Notary: Intention to sell");
        menu.addEntry("To Notary: Get State of Good");
        menu.addEntry("To Notary: Transfer Good");
        menu.addEntry("To User  : Buy Good");
        menu.addEntry("Exit");
        menu.addEntry("DEBUG");

        option = menu.display();

        int good, buyer;
        boolean response;


            try {
                notaryInterface = (NotaryInterface) Naming.lookup(NOTARY_URI);
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

                    response = notaryInterface.intentionToSell(1, 2, intention);
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


                // === GET STATE OF GOOD ===
                case 3:
                    good =  Integer.parseInt(new BoxUI("What is the good ID to transfer?").showAndGet());
                    buyer = Integer.parseInt(new BoxUI("What is the buyer ID?").showAndGet());

                    try {
                        response = notaryInterface.transferGood(userID, buyer, good);
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
    }
}
