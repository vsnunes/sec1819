package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;

import java.rmi.RemoteException;

public class TransferGood extends Operation {

    public TransferGood(ClientInterface ci, NotaryInterface ni) {
        super("TransferGood", ci, ni);
    }

    @Override
    public boolean getAndCheckArgs() {
        try {
            args.add(Integer.parseInt(new BoxUI(REQUEST_GOODID).showAndGet()));
            args.add(Integer.parseInt(new BoxUI(REQUEST_BUYER).showAndGet()));

            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean execute() {
        boolean response;

        int good = (int)args.get(0);
        int buyer = (int)args.get(1);

        try {
            response = notaryInterface.transferGood(ClientService.userID, buyer, good);

            if (response == true) {
                new BoxUI(CLIENT_SUCCESS_TRANSFER).show(BoxUI.GREEN_BOLD);
            } else new BoxUI(CLIENT_TRANSFER_PROBLEM).show(BoxUI.RED_BOLD);

            return response;

        } catch(TransactionException e) {
            new BoxUI(NOTARY_REPORT_PROBLEM + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
        }
        catch (RemoteException e) {
            new BoxUI(NOTARY_CONN_PROBLEM).show(BoxUI.RED_BOLD_BRIGHT);
        }


        return false;

    }
}
