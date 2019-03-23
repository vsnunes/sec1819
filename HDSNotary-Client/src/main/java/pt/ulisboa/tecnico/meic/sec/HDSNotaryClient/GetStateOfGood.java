package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.rmi.RemoteException;

public class GetStateOfGood extends Operation {

    public GetStateOfGood(ClientInterface ci, NotaryInterface ni) {
        super("GetStateOfGood", ci, ni);
    }

    @Override
    public boolean getAndCheckArgs() {
        try {
            args.add(Integer.parseInt(new BoxUI(REQUEST_GOODID).showAndGet()));

            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean execute() {
        boolean response;

        int good = (int)args.get(0);

        try {
            response = notaryInterface.getStateOfGood(good);

            if (response == true) {
                new BoxUI(INFO_ITEM_FORSALE).show(BoxUI.GREEN_BOLD);
            } else new BoxUI(INFO_ITEM_NOTFORSALE).show(BoxUI.RED_BOLD);

            return response;

        }
        catch(GoodException e) {
            new BoxUI(NOTARY_REPORT_PROBLEM + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
        }
        catch (RemoteException e) {
            new BoxUI(NOTARY_CONN_PROBLEM).show(BoxUI.RED_BOLD_BRIGHT);
        }


        return false;
    }
}
