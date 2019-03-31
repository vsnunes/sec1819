package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;

public class ClientBoxStats implements ClientVisitor {

    /** Notary error messages verbose **/
    public static final String NOTARY_REPORT_PROBLEM = "Notary report the following problem: ";
    public static final String NOTARY_CONN_PROBLEM = "There were a problem in connecting to Notary!";

    public static final String CLIENT_NOTBOUND_PROBLEM = ":( NotBound on Client! ";
    public static final String CLIENT_MALFOURL_PROBLEM = ":( Malform URL! Cannot find Client Service! ";
    public static final String CLIENT_CONNLOST_PROBLEM = ":( It looks like I miss the connection with Client! ";
    public static final String CLIENT_TRANSFER_PROBLEM = "There was an error on the transferring process! ";
    public static final String CLIENT_DIGEST_PROBELM = "Digest not created ";
    public static final String CLIENT_SECURITY_PROBLEM = "Problem using security methods ";
    public static final String CLIENT_BUYGOOD_OK = "Successfully bought item";
    public static final String CLIENT_BUYGOOD_NOTOK = "Error while buying the item";
    public final static String INFO_TAMP = "Tampering detected";
    public final static String INFO_REPLAY = "Replay attack detected";



    public static final String CLIENT_SUCCESS_TRANSFER = "Successfully transferred good!";

    public static final String INFO_ITEM_FORSALE = "The item is now for sale!";
    public static final String INFO_ITEM_NOTFORSALE = "The item is now NOT for sale!";
    public static final String INFO_ITEM_INT_FORSALE = "The item is now marked for sale!";
    public static final String INFO_ITEM_INT_NOTFORSALE = "The item is now market NOT for sale!";

    /**
     * Displays generic errors for all operations
     * @param operation
     * @return true if an error occur or false otherwise
     */
    @Override
    public boolean check4Failures(Operation operation) {
        switch(operation.getStatus()) {
            case FAILURE_NOTARY_REPORT:
                new BoxUI(NOTARY_REPORT_PROBLEM + operation.getStatusVerbose()).show(BoxUI.RED_BOLD_BRIGHT);
                return true;

            case FAILURE_NOTARY_CONN:
                new BoxUI(NOTARY_CONN_PROBLEM + operation.getStatusVerbose()).show(BoxUI.RED_BOLD_BRIGHT);
                return true;

            case FAILURE_DIGEST:
                new BoxUI(CLIENT_DIGEST_PROBELM + operation.getStatusVerbose()).show(BoxUI.RED_BOLD_BRIGHT);
                return true;

            case FAILURE_SECURITY:
                new BoxUI(CLIENT_SECURITY_PROBLEM + operation.getStatusVerbose()).show(BoxUI.RED_BOLD_BRIGHT);
                return true;

            case FAILURE_TRANSACTION:
                new BoxUI(NOTARY_REPORT_PROBLEM + operation.getStatusVerbose()).show(BoxUI.RED_BOLD_BRIGHT);
                return true;

            case FAILURE_NOT_BOUND:
                new BoxUI(CLIENT_NOTBOUND_PROBLEM).show(BoxUI.RED_BOLD_BRIGHT);
                return true;

            case FAILURE_MAL_FORM_URL:
                new BoxUI(CLIENT_MALFOURL_PROBLEM).show(BoxUI.RED_BOLD_BRIGHT);
                return true;

            case FAILURE_CONN_LOST:
                new BoxUI(CLIENT_CONNLOST_PROBLEM).show(BoxUI.RED_BOLD_BRIGHT);
                return true;

            case FAILURE_GOOD:
                new BoxUI(NOTARY_REPORT_PROBLEM + operation.getStatusVerbose()).show(BoxUI.RED_BOLD_BRIGHT);
                return true;

            case FAILURE_TAMP:
                new BoxUI(INFO_TAMP ).show(BoxUI.RED_BOLD_BRIGHT);
                return true;

            case FAILURE_REPLAY:
                new BoxUI(INFO_REPLAY).show(BoxUI.RED_BOLD_BRIGHT);
                return true;
        }

        return false;
    }

    @Override
    public void accept(BuyGood operation) {
        if (operation.getStatus() == Operation.Status.SUCCESS) {
            new BoxUI(CLIENT_BUYGOOD_OK).show(BoxUI.GREEN_BOLD);
        }
        else {
            new BoxUI(CLIENT_BUYGOOD_NOTOK).show(BoxUI.RED_BOLD_BRIGHT);
        }
    }

    @Override
    public void accept(GetStateOfGood operation) {
        if (operation.getStatus() == Operation.Status.SUCCESS) {
            new BoxUI(INFO_ITEM_FORSALE).show(BoxUI.GREEN_BOLD);
        } else new BoxUI(INFO_ITEM_NOTFORSALE).show(BoxUI.RED_BOLD);

    }

    @Override
    public void accept(IntentionToSell operation) {
        if (operation.getStatus() == Operation.Status.SUCCESS) {
            new BoxUI(INFO_ITEM_INT_FORSALE).show(BoxUI.GREEN_BOLD);
        } else new BoxUI(INFO_ITEM_INT_NOTFORSALE).show(BoxUI.RED_BOLD);
    }

    @Override
    public void accept(TransferGood operation) {
        if (operation.getStatus() == Operation.Status.SUCCESS) {
            new BoxUI(CLIENT_SUCCESS_TRANSFER).show(BoxUI.GREEN_BOLD);
        } else new BoxUI(CLIENT_TRANSFER_PROBLEM).show(BoxUI.RED_BOLD);
    }

    @Override
    public void accept(Debug operation) {

    }

    @Override
    public void accept(GetBadStateOfGood operation){
        if (operation.getStatus() == Operation.Status.SUCCESS) {
            new BoxUI(INFO_ITEM_FORSALE).show(BoxUI.GREEN_BOLD);
        } else new BoxUI(INFO_ITEM_NOTFORSALE).show(BoxUI.RED_BOLD);
    }

    @Override
    public void accept(ReplayAttack operation){
        if (operation.getStatus() == Operation.Status.SUCCESS) {
            new BoxUI(INFO_ITEM_FORSALE).show(BoxUI.GREEN_BOLD);
        } else new BoxUI(INFO_ITEM_NOTFORSALE).show(BoxUI.RED_BOLD);
    }

}
