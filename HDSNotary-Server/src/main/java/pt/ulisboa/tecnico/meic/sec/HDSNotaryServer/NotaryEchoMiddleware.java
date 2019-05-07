package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.*;

import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.Main.NOTARY_SERVICE_PORT;
import static pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.Main.USERS_CERTS_FOLDER;
import static pt.ulisboa.tecnico.meic.sec.util.CertificateHelper.*;

import java.io.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class NotaryEchoMiddleware implements NotaryInterface {

    //TODO: ArrayList of NotaryInterface with all RMI proxy objects
    private ArrayList<NotaryInterface> servers;


    public NotaryEchoMiddleware(String pathToServersCfg, String myUrl) {
        this.servers = new ArrayList<>();
        servers = new ArrayList<>();

        List<String> urls = CFGHelper.fetchURLsFromCfg(pathToServersCfg,0);

        for (String url : urls) {
            try {
                if(!url.equals(myUrl)) {
                    servers.add((NotaryInterface) Naming.lookup(url));
                }
            } catch (NotBoundException e) {
                throw new NotaryEchoMiddlewareException(":( NotBound on Notary at " + url);
            } catch (MalformedURLException e) {
                throw new NotaryEchoMiddlewareException(":( Malform URL! Cannot find Notary Service at " + url);
            } catch (RemoteException e) {
                System.out.println(":( It looks like I miss the connection with Notary at " + url + " ignoring...");
            }
        }
    }

    @Override
    public Interaction intentionToSell(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        
        
        /*
        Just to init
            sentecho = false
            delivered = false
            echos[numero de notarios]
        TODO: Arrays of responses
        for each notary in array do
            responses <= notary.intentionToSell(request);

        If I'm the one who sent this request AND not yet sent ECHO then
            setecho <= true
            for each notary in array do
                echos <= notary.echo(request)
            
            Esta parte que esta aqui em baixo é basicamente usar future tasks para quando existir um echo response, e quando existir echo quorum responses
            saio do loop, meto delivered a true e envio m

            #OfEchos = 0
            for each process P in echos
            if echo[P] is not null then
                #OfEchos <= #OfEchos + 1

            if #OfEchos > (N + f) / 2 then
                return RESPONSE
            else wait for more echos
         
         */
        
        return null;
    }

    //TODO: Add this to the interface
    @Override
    public Interaction echo(Interaction request) {
        /*
        TODO: Maintain a list of received echos.
        TODO: Add to the interaction the owner of the request (which notary sent that 
        request in order to distinguish processes)

        P <= request.getNotaryID() //P stores the ID of the Notary who sent this echo

        if I'm not yet received echo from process P then
            echos[P] = request
          
        */
    }

    @Override
    public Interaction getStateOfGood(Interaction request) throws RemoteException, GoodException, HDSSecurityException {
        return null;
    }

    @Override
    public Interaction transferGood(Interaction request) throws RemoteException, TransactionException, GoodException, HDSSecurityException {
        return null;
    }

    @Override
    public int getClock(int userID) throws RemoteException {
        return 0;
    }

    @Override
    public void doPrint() throws RemoteException {

    }

    @Override
    public void shutdown() throws RemoteException {

    }
}