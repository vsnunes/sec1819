package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.Digest;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;
import pt.ulisboa.tecnico.meic.sec.util.VirtualCertificate;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public class BuyGood extends Operation {

    public BuyGood(ClientInterface ci, NotaryInterface ni) {
        super("BuyGood", ci, ni);
    }

    @Override
    public boolean getAndCheckArgs() {
        try {
            String clientID = new BoxUI("What is the client ID?").showAndGet();
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

            Interaction request = new Interaction();
            request.setGoodID(good);
            request.setBuyerID(ClientService.userID);

            VirtualCertificate cert = new VirtualCertificate();
            cert.init(new File("../HDSNotaryLib/src/main/resources/certs/user" + ClientService.userID + ".crt").getAbsolutePath(),
                    new File("../HDSNotaryLib/src/main/resources/certs/java_certs/private_user" + ClientService.userID + "_pkcs8.pem").getAbsolutePath());


            request.setBuyerClock(notaryInterface.getClock(ClientService.userID));
            String data = "" + good + ClientService.userID + notaryInterface.getClock(ClientService.userID);
            request.setBuyerHMAC(Digest.createDigest(data, cert));

            response = anotherClient.buyGood(request);
            return response;
        } catch (RemoteException e) {
            new BoxUI(CLIENT_CONNLOST_PROBLEM).show(BoxUI.RED_BOLD_BRIGHT);
        } catch (NoSuchAlgorithmException e) {
            new BoxUI(CLIENT_NO_ALGORITHM + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
        } catch (HDSSecurityException e) {
            new BoxUI(CLIENT_SECURITY_PROBLEM + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
        }

        //DO NOT BLOCK THIS THREAD
        /*if (response == true) {
            new BoxUI("Successfully bought good!").show(BoxUI.GREEN_BOLD);
        } else new BoxUI("Seller didn't sell the good!").show(BoxUI.RED_BOLD);*/
        return false;
    }
}
