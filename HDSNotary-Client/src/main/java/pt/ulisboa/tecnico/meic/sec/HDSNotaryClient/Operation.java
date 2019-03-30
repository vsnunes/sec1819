package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class to describe a Client Operation
 */
public abstract class Operation {

    /** Arguments requests verbose **/
    public static final String REQUEST_GOODID = "What is the good ID?";
    public static final String REQUEST_TOSELL = "To sell?";
    public static final String REQUEST_BUYER = "What is the buyer ID?";

    /** Notary error messages verbose **/
    public static final String NOTARY_REPORT_PROBLEM = "Notary report the following problem: ";
    public static final String NOTARY_CONN_PROBLEM = "There were a problem in connecting to Notary!";

    public static final String CLIENT_NOTBOUND_PROBLEM = ":( NotBound on Client! ";
    public static final String CLIENT_MALFOURL_PROBLEM = ":( Malform URL! Cannot find Client Service! ";
    public static final String CLIENT_CONNLOST_PROBLEM = ":( It looks like I miss the connection with Client! ";
    public static final String CLIENT_TRANSFER_PROBLEM = "There was an error on the transferring process! ";
    public static final String CLIENT_DIGEST_PROBELM = "Digest not created ";
    public static final String CLIENT_SECURITY_PROBLEM = "Problem using security methods ";
    public static final String CLIENT_NO_ALGORITHM = "No such algorithm: ";

    public static final String CLIENT_SUCCESS_TRANSFER = "Successfully transferred good!";

    public static final String INFO_ITEM_FORSALE = "The item is now for sale!";
    public static final String INFO_ITEM_NOTFORSALE = "The item is now NOT for sale!";

    /** Security problems verbose reports **/
    public static final String NOTARY_REPORT_TAMPERING = "Tampering detected!";
    public static final String NOTARY_REPORT_DUP_MSG = "Replay attack detected!!";



    protected String name;

    protected List<Object> args;

    protected ClientInterface clientInterface;

    protected NotaryInterface notaryInterface;

    public Operation(String name) {
        this.name = name;
        this.args = new ArrayList<>();
    }

    public Operation(String name, ClientInterface clientInterface) {
        this.name = name;
        this.args = new ArrayList<>();
        this.clientInterface = clientInterface;
    }

    public Operation(String name, NotaryInterface notaryInterface) {
        this.name = name;
        this.args = new ArrayList<>();
        this.notaryInterface = notaryInterface;
    }

    public Operation(String name, ClientInterface clientInterface, NotaryInterface notaryInterface) {
        this.name = name;
        this.args = new ArrayList<>();
        this.clientInterface = clientInterface;
        this.notaryInterface = notaryInterface;
    }

    public Operation(String name, List<Object> args) {
        this.name = name;
        this.args = args;
    }

    /**
     * Validates the arguments before execute
     * @return true if args are correct false otherwise
     */
    public abstract boolean getAndCheckArgs();

    /**
     * Executes the client operation
     * @return
     */
    public abstract boolean execute();

}
