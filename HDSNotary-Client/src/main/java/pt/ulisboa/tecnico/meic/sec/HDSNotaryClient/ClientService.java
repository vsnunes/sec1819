package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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
    public boolean buyGood(int goodId, int buyerId) throws RemoteException {
        boolean response;

        //Call transferGood of Notary
        try {
            response = notaryInterface.transferGood(userID, buyerId, goodId);

            if (response == true) {
                new BoxUI("Successfully transferred good!").show(BoxUI.GREEN_BOLD);
            } else new BoxUI("There was an error on the transferring process!").show(BoxUI.RED_BOLD);

            return response;
        } catch (RemoteException e) {
            new BoxUI("There were a problem in connecting to Notary!").show(BoxUI.RED_BOLD_BRIGHT);


        } catch (TransactionException e) {
            new BoxUI("Notary report the following problem: " + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);

        }

        return false;
    }
}
