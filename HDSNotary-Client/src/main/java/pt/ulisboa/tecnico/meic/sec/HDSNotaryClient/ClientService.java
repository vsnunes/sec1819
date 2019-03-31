package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.Digest;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;
import pt.ulisboa.tecnico.meic.sec.util.VirtualCertificate;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;

import static pt.ulisboa.tecnico.meic.sec.HDSNotaryClient.Operation.NOTARY_REPORT_DUP_MSG;
import static pt.ulisboa.tecnico.meic.sec.HDSNotaryClient.Operation.NOTARY_REPORT_TAMPERING;

public class ClientService extends UnicastRemoteObject implements ClientInterface, Serializable {

    /** URI Of Notary **/
    public static String NOTARY_URI = "//localhost:10000/HDSNotary";

    public static NotaryInterface notaryInterface;

    public static int userID = 1;

    /** Port for accepting clients connection to the service **/
    public static int CLIENT_SERVICE_PORT = 10000 + userID;
    public static String CLIENT_SERVICE_NAME = "Client" + userID;

    /** Instance of ClientService the one will allow others client to connect to. **/
    private static ClientService instance;

    protected ClientService() throws RemoteException {
        super();

        try {
            notaryInterface = (NotaryInterface) Naming.lookup(NOTARY_URI);
        } catch (NotBoundException e) {
            new BoxUI(":( NotBound on Notary!").show(BoxUI.RED_BOLD_BRIGHT);
        } catch (MalformedURLException e) {
            new BoxUI(":( Malform URL! Cannot find Notary Service!").show(BoxUI.RED_BOLD_BRIGHT);
        } catch (RemoteException e) {
            new BoxUI(":( It looks like I miss the connection with Notary!").show(BoxUI.RED_BOLD_BRIGHT);
        }
    }

    public static ClientService getInstance() throws RemoteException {
        if(instance == null){
            return new ClientService();
        }
        return instance;
    }

    @Override
    public Interaction buyGood(Interaction request) throws RemoteException {
        int goodId = request.getGoodID();
        int buyerId = request.getBuyerID();


        Interaction response;

        //Call transferGood of Notary
        try {
            request.setSellerID(userID);
            request.setBuyerID(buyerId);
            request.setGoodID(goodId);
            request.setSellerClock(notaryInterface.getClock(userID));
            request.setBuyerClock(notaryInterface.getClock(buyerId));

            VirtualCertificate cert = new VirtualCertificate();
            cert.init("", new File(System.getProperty("project.user.private.path") +
                    ClientService.userID + System.getProperty("project.user.private.ext")).getAbsolutePath());

            //TODO verify buyer HMAC
            String data = "" + request.getSellerID() + request.getBuyerID() + request.getGoodID() + request.getSellerClock() + request.getBuyerClock();
            request.setSellerHMAC(Digest.createDigest(data, cert));

            response = notaryInterface.transferGood(request);
            /*checks answer from notary*/
            cert = new VirtualCertificate();
            try {
                cert.init(new File("../HDSNotaryLib/src/main/resources/certs/rootca.crt").getAbsolutePath(),
                        new File("../HDSNotaryLib/src/main/resources/certs/java_certs/private_rootca_pkcs8.pem" ).getAbsolutePath());
            } catch (HDSSecurityException e) {
                e.printStackTrace();
            }
            /*compare hmacs*/
            if(Digest.verify(response, cert) == false){
                throw new HDSSecurityException(NOTARY_REPORT_TAMPERING);
            }

            /*check freshness*/
            if(request.getSellerClock() != response.getSellerClock()){
                throw new HDSSecurityException(NOTARY_REPORT_DUP_MSG);
            }

            /*build a new hmac*/
            cert = new VirtualCertificate();
            cert.init(new File("../HDSNotaryLib/src/main/resources/certs/user" + ClientService.userID + ".crt").getAbsolutePath(),
                    new File("../HDSNotaryLib/src/main/resources/certs/java_certs/private_user" + ClientService.userID + "_pkcs8.pem").getAbsolutePath());

            data = "" + response.getSellerID() + response.getBuyerID() + response.getGoodID() + response.getSellerClock() + response.getBuyerClock();
            request.setSellerHMAC(Digest.createDigest(data, cert));

            return response;

        } catch (RemoteException e) {
            new BoxUI("There were a problem in connecting to Notary!").show(BoxUI.RED_BOLD_BRIGHT);
        } catch (TransactionException e) {
            new BoxUI("Notary report the following problem: " + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
        } catch (NoSuchAlgorithmException e) {
            new BoxUI("No such algorithm: " + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
        } catch (HDSSecurityException e) {
            new BoxUI("Security problem: " + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
        } catch (GoodException e) {
            new BoxUI(e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
        }

        return null;
    }
}
