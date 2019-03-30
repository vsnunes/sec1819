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

    /** Security problems verbose reports **/
    public static final String NOTARY_REPORT_TAMPERING = "Tampering detected!";
    public static final String NOTARY_REPORT_DUP_MSG = "Replay attack detected!!";

    /** Possible Operation status **/
    public enum Status {NOT_EXECUTED, SUCCESS, FAILURE, FAILURE_NOTARY_REPORT, FAILURE_NOTARY_CONN, FAILURE_DIGEST,
                        FAILURE_SECURITY, FAILURE_TRANSACTION, FAILURE_NOT_BOUND, FAILURE_MAL_FORM_URL, FAILURE_CONN_LOST,
                        FAILURE_GOOD, FAILURE_TAMP, FAILURE_REPLAY}


    protected String name;

    protected List<Object> args;

    protected ClientInterface clientInterface;

    protected NotaryInterface notaryInterface;

    /** Status of this operation **/
    protected Status status;
    /** A verbose to display why the operation is on this state **/
    protected String statusVerbose;

    public Operation(String name) {
        this.name = name;
        this.args = new ArrayList<>();
        this.status = Status.NOT_EXECUTED;
    }

    public Operation(String name, ClientInterface clientInterface) {
        this.name = name;
        this.args = new ArrayList<>();
        this.clientInterface = clientInterface;
        this.status = Status.NOT_EXECUTED;
    }

    public Operation(String name, NotaryInterface notaryInterface) {
        this.name = name;
        this.args = new ArrayList<>();
        this.notaryInterface = notaryInterface;
        this.status = Status.NOT_EXECUTED;
    }

    public Operation(String name, ClientInterface clientInterface, NotaryInterface notaryInterface) {
        this.name = name;
        this.args = new ArrayList<>();
        this.clientInterface = clientInterface;
        this.notaryInterface = notaryInterface;
        this.status = Status.NOT_EXECUTED;
    }

    public Operation(String name, List<Object> args) {
        this.name = name;
        this.args = args;
        this.status = Status.NOT_EXECUTED;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    public void setStatus(boolean result) {
        if (result == true)
            this.status = Status.SUCCESS;
        else this.status = Status.FAILURE;
    }

    public void setStatus(Status status, String statusVerbose) {
        this.status = status;
        this.statusVerbose = statusVerbose;
    }


    public void setStatus(boolean result, String statusVerbose) {
        if (result == true)
            this.status = Status.SUCCESS;
        else this.status = Status.FAILURE;

        this.statusVerbose = statusVerbose;
    }

    public String getStatusVerbose() {
        return statusVerbose;
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
    public abstract void execute();

    public abstract void visit(ClientVisitor visitor);

}
