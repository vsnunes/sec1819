package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryClient.exceptions.NotaryMiddlewareException;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.Certification;
import pt.ulisboa.tecnico.meic.sec.util.Digest;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;
import pt.ulisboa.tecnico.meic.sec.util.VirtualCertificate;

import java.io.File;
import java.io.IOException;
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

    /** Certification Method used by Notary **/
    public static boolean NOTARY_USES_VIRTUAL = true;

    public static NotaryInterface notaryInterface;

    public static int userID = 1;

    /** Port for accepting clients connection to the service **/
    public static int CLIENT_SERVICE_PORT = 10010 + userID;
    public static String CLIENT_SERVICE_NAME = "Client" + userID;

    /** Instance of ClientService the one will allow others client to connect to. **/
    private static ClientService instance;

    protected ClientService() throws RemoteException, NotaryMiddlewareException, IOException {
        super();


        notaryInterface = new NotaryMiddleware(System.getProperty("project.nameserver.config"));

    }

    public static ClientService getInstance() throws RemoteException, NotaryMiddlewareException, IOException {
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



            /*verify answer from Buyer*/
            VirtualCertificate cert = new VirtualCertificate();
            try {
                cert.init(new File(System.getProperty("project.users.cert.path") + buyerId + System.getProperty("project.users.cert.ext")).getAbsolutePath());
            } catch (HDSSecurityException e) {
                e.printStackTrace();
            }

            try {
                /*compare hmacs*/

                String data = "" + request.getGoodID() + request.getBuyerID() + request.getBuyerClock() + request.getSellerClock();
                if(!Digest.verify(request.getBuyerHMAC(), data, cert)){
                    throw new GoodException("ClientService: Tampering detected in Buyer!");
                }
                /*check freshness*/
                if(request.getBuyerClock() <= notaryInterface.getClock(buyerId)){
                    throw new GoodException("ClientService: Replay attack detected in Buyer!!");
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (HDSSecurityException e) {
                e.printStackTrace();
            }

            request.setSellerID(userID);
            request.setSellerClock(notaryInterface.getClock(userID) + 1);

            /*build seller hmac*/
            cert = new VirtualCertificate();
            cert.init("", new File(System.getProperty("project.user.private.path") +
                    ClientService.userID + System.getProperty("project.user.private.ext")).getAbsolutePath());

            String data = "" + request.getSellerID() + request.getBuyerID() + request.getGoodID() + request.getSellerClock() + request.getBuyerClock();
            request.setSellerHMAC(Digest.createDigest(data, cert));
            response = notaryInterface.transferGood(request);
            if(response == null) {
                throw new GoodException("Byzantine quorum not achieved :(");
            }

            //Check the MAC using the cert of a corresponded Notary
            System.setProperty("project.notary.cert.path", "../HDSNotaryLib/src/main/resources/certs/notary" + response.getNotaryID() + ".crt");

            /*checks answer from notary*/
            Certification notaryCert = new VirtualCertificate();
            notaryCert.init(new File(System.getProperty("project.notary.cert.path")).getAbsolutePath());

            /*compare hmacs*/
            if(Digest.verify(response, notaryCert) == false){
                throw new HDSSecurityException(NOTARY_REPORT_TAMPERING);
            }

            /*check freshness*/
            if(request.getSellerClock() != response.getSellerClock()){
                throw new HDSSecurityException(NOTARY_REPORT_DUP_MSG);
            }

            /*build a new hmac*/
            cert = new VirtualCertificate();
            cert.init("", new File(System.getProperty("project.user.private.path") +
                    ClientService.userID + System.getProperty("project.user.private.ext")).getAbsolutePath());

            data = "" + response.getSellerID() + response.getBuyerID() + response.getGoodID() + response.getSellerClock() + response.getBuyerClock();
            response.setSellerHMAC(Digest.createDigest(data, cert));

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
        } catch (NullPointerException e) {
            System.out.println("Amplification was probably triggered on server - some responses from notary might be " +
                    "be missing");
        }

        return null;
    }
}
