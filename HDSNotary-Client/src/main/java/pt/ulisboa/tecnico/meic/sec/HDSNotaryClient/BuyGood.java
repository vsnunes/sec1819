package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class BuyGood extends Operation {

    public BuyGood(ClientInterface ci, NotaryInterface ni) {
        super("BuyGood", ci, ni);
    }

    @Override
    public boolean getAndCheckArgs() {
        try {
            String clientID = new BoxUI("What is the client URL?").showAndGet();
            args.add("//localhost:1000" + clientID + "/Client" + clientID);
            args.add(Integer.parseInt(new BoxUI("What is the good ID to buy?").showAndGet()));
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean execute() {
        ClientInterface anotherClient;
        boolean response;

        String clientURL = (String) args.get(0);
        int good = (int) args.get(1);

        try {
            anotherClient = (ClientInterface) Naming.lookup(clientURL);


        } catch (NotBoundException e) {
            new BoxUI(CLIENT_NOTBOUND_PROBLEM).show(BoxUI.RED_BOLD_BRIGHT);
            return false;

        } catch (MalformedURLException e) {
            new BoxUI(CLIENT_MALFOURL_PROBLEM).show(BoxUI.RED_BOLD_BRIGHT);
            return false;
        } catch (Exception e) {
            new BoxUI(CLIENT_CONNLOST_PROBLEM).show(BoxUI.RED_BOLD_BRIGHT);
            return false;
        }

        try {
            response = anotherClient.buyGood(good, ClientService.userID);
            return response;
        } catch (RemoteException e) {
            new BoxUI(CLIENT_CONNLOST_PROBLEM).show(BoxUI.RED_BOLD_BRIGHT);
        }

        //DO NOT BLOCK THIS THREAD
        /*if (response == true) {
            new BoxUI("Successfully bought good!").show(BoxUI.GREEN_BOLD);
        } else new BoxUI("Seller didn't sell the good!").show(BoxUI.RED_BOLD);*/
        return false;
    }
}
