package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.Digest;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;
import pt.ulisboa.tecnico.meic.sec.util.VirtualCertificate;

import java.io.File;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

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
            Interaction request = new Interaction();
            request.setUserID(ClientService.userID);
            request.setGoodID(good);

            VirtualCertificate cert = new VirtualCertificate();
            cert.init(new File("../HDSNotaryLib/src/main/resources/certs/user" + ClientService.userID + ".crt").getAbsolutePath(),
                    new File("../HDSNotaryLib/src/main/resources/certs/java_certs/private_user" + ClientService.userID + "_pkcs8.pem").getAbsolutePath());


            request.setHmac(Digest.createDigest(request, cert));

            response = notaryInterface.getStateOfGood(request);

            if (response == true) {
                new BoxUI(INFO_ITEM_FORSALE).show(BoxUI.GREEN_BOLD);
            } else new BoxUI(INFO_ITEM_NOTFORSALE).show(BoxUI.RED_BOLD);

            return response;

        }
        catch(GoodException e) {
            new BoxUI(NOTARY_REPORT_PROBLEM + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
        }
        catch (RemoteException e) {
            new BoxUI(NOTARY_CONN_PROBLEM + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
        } catch (NoSuchAlgorithmException e) {
            new BoxUI(CLIENT_DIGEST_PROBELM + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
        } catch (HDSSecurityException e) {
            new BoxUI(CLIENT_SECURITY_PROBLEM + e.getMessage()).show(BoxUI.RED_BOLD_BRIGHT);
        }


        return false;
    }
}
