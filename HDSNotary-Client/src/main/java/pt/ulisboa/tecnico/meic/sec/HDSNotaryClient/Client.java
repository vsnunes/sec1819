package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {

    private static final String NOTARY_URI = "//localhost:10000/HDSNotary";

    private static NotaryInterface notaryInterface;

    public static void main(String[] args){
        try{
            notaryInterface = (NotaryInterface) Naming.lookup(NOTARY_URI);

            boolean response = notaryInterface.intentionToSell(1,2, true);

            System.out.println("If you see false then is correct: " + response);

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
        }
    }
}
