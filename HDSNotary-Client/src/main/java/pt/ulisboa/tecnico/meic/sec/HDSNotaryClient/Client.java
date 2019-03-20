package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
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

    private int userID = 1;

    public static void main(String[] args){

        MenuUI menu = new MenuUI("User client");

        menu.addEntry("To Notary: Intention to sell");
        menu.addEntry("To Notary: Get State of Good");
        menu.addEntry("To Notary: Transfer Good");
        menu.addEntry("To User  : Buy Good");
        menu.addEntry("Exit");

        int option = menu.display();

        int good;
        boolean response;

        try{
            notaryInterface = (NotaryInterface) Naming.lookup(NOTARY_URI);


            switch (option) {
                // === INTENTION TO SELL ===
                case 1:
                    good = Integer.parseInt(new BoxUI("What is the good ID?").showAndGet());
                    boolean intention = Boolean.parseBoolean(new BoxUI("To sell?").showAndGet());

                    response = notaryInterface.intentionToSell(1,2, intention);

                    if (response == true) {
                        new BoxUI("The item is now for sale!").show(BoxUI.GREEN_BOLD);
                    }

                    break;

                case 2:
                    good = Integer.parseInt(new BoxUI("What is the good ID?").showAndGet());

                    response = notaryInterface.getStateOfGood(good);

                    if (response == true) {
                        new BoxUI("The item is for sale!").show(BoxUI.GREEN_BOLD);
                    }
                    else new BoxUI("The item is NOT for sale!").show(BoxUI.RED_BOLD);

                    break;
            }


        }catch(MalformedURLException e){
            System.err.println("URL is not formed correctly");
            System.exit(-1);
        }catch(RemoteException e){
            System.err.println("Client could not connect with Server: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }catch(NotBoundException e){
            System.err.println("Service not bound on provided URL");
            System.exit(-1);
        }catch (GoodException e){}
    }
}
