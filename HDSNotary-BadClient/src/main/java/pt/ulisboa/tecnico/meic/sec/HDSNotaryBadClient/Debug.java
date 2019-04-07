package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.rmi.RemoteException;

public class Debug extends Operation {

    public Debug(ClientInterface ci, NotaryInterface ni) {
        super("Debug", ci, ni);
    }

    @Override
    public boolean getAndCheckArgs() {
        //there are no arguments so always good to go :)
        return true;
    }

    @Override
    public void execute() {
        try {
            notaryInterface.doPrint();
            setStatus(Status.SUCCESS);
        } catch (RemoteException e) {
            setStatus(Status.FAILURE_NOTARY_REPORT, e.getMessage());
        }
    }

    @Override
    public void visit(ClientVisitor visitor) {
        visitor.accept(this);
    }
}
