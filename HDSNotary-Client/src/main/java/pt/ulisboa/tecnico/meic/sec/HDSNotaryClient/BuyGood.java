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
    private String clientID;
    public BuyGood(ClientInterface ci, NotaryInterface ni) {
        super("BuyGood", ci, ni);
    }

    @Override
    public boolean getAndCheckArgs() {
        try {
            clientID = new BoxUI("What is the client ID?").showAndGet();
            args.add("//localhost:1000" + clientID + "/Client" + clientID);
            args.add(Integer.parseInt(new BoxUI("What is the good ID to buy?").showAndGet()));
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void execute() {
        ClientInterface anotherClient;
        Interaction response;

        String clientURL = (String) args.get(0);
        int good = (int) args.get(1);

        try {
            anotherClient = (ClientInterface) Naming.lookup(clientURL);


        } catch (NotBoundException e) {
            setStatus(Status.FAILURE_NOTARY_REPORT, e.getMessage());
            return;

        } catch (MalformedURLException e) {
            setStatus(Status.FAILURE_MAL_FORM_URL, e.getMessage());
            return;
        } catch (Exception e) {
            setStatus(Status.FAILURE_CONN_LOST, e.getMessage());
            return;
        }

        try {

            Interaction request = new Interaction();
            request.setGoodID(good);
            request.setBuyerID(ClientService.userID);

            VirtualCertificate cert = new VirtualCertificate();
            cert.init(new File("../HDSNotaryLib/src/main/resources/certs/user" + ClientService.userID + ".crt").getAbsolutePath(),
                    new File("../HDSNotaryLib/src/main/resources/certs/java_certs/private_user" + ClientService.userID + "_pkcs8.pem").getAbsolutePath());


            request.setBuyerClock(notaryInterface.getClock(ClientService.userID));
            request.setSellerClock(notaryInterface.getClock(Integer.parseInt(clientID)));

            String data = "" + good + ClientService.userID + notaryInterface.getClock(ClientService.userID) + notaryInterface.getClock(Integer.parseInt(clientID));
            request.setBuyerHMAC(Digest.createDigest(data, cert));

            response = anotherClient.buyGood(request);
            if(response != null) {
                /*checks answer from notary*/
                cert = new VirtualCertificate();
                try {
                    cert.init(new File("../HDSNotaryLib/src/main/resources/certs/rootca.crt").getAbsolutePath(),
                            new File("../HDSNotaryLib/src/main/resources/certs/java_certs/private_rootca_pkcs8.pem").getAbsolutePath());
                } catch (HDSSecurityException e) {
                    e.printStackTrace();
                }
                /*compare hmacs*/
                if (Digest.verify(response, cert) == false) {
                    throw new HDSSecurityException(NOTARY_REPORT_TAMPERING);
                }

                /*verify seller*/
                cert = new VirtualCertificate();
                try {
                    cert.init(new File("../HDSNotaryLib/src/main/resources/certs/user" + response.getSellerID() + ".crt").getAbsolutePath(),
                            new File("../HDSNotaryLib/src/main/resources/certs/java_certs/private_user" + response.getSellerID() + "_pkcs8.pem" ).getAbsolutePath());
                } catch (HDSSecurityException e) {
                    e.printStackTrace();
                }

                try {
                    /*compare hmacs*/
                    data = "" + response.getSellerID() + response.getBuyerID() + response.getGoodID() + response.getSellerClock() + response.getBuyerClock();
                    if(!Digest.verify(response.getSellerHMAC(), data, cert)){
                        setStatus(Status.FAILURE_TAMP);
                        return;
                        //throw new GoodException("Tampering detected in Seller!");
                    }
                    /*check freshness*/
                    if(request.getSellerClock() != response.getSellerClock()){
                        setStatus(Status.FAILURE_REPLAY);
                        return;
                        //throw new GoodException("Replay attack detected in Seller!!");
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (HDSSecurityException e) {
                    e.printStackTrace();
                }


                setStatus(response.getResponse());
            } else{
                setStatus(false);
            }

        } catch (RemoteException e) {
            setStatus(Status.FAILURE_NOTARY_REPORT, e.getMessage());

        } catch (NoSuchAlgorithmException e) {
            setStatus(Status.FAILURE_DIGEST, e.getMessage());

        } catch (HDSSecurityException e) {
            setStatus(Status.FAILURE_SECURITY, e.getMessage());
        }

        //DO NOT BLOCK THIS THREAD
        /*if (response == true) {
            new BoxUI("Successfully bought good!").show(BoxUI.GREEN_BOLD);
        } else new BoxUI("Seller didn't sell the good!").show(BoxUI.RED_BOLD);*/
    }

    @Override
    public void visit(ClientVisitor visitor) {
        visitor.accept(this);
    }
}
