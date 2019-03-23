package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
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
    public boolean execute() {
        try {
            notaryInterface.doPrint();
            return true;
        } catch (RemoteException e) {
            new BoxUI(NOTARY_CONN_PROBLEM).show(BoxUI.RED_BOLD_BRIGHT);
            return false;
        }
    }
}
