package pt.ulisboa.tecnico.meic.sec.HDSNotaryBadClient;

import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.util.Certification;
import pt.ulisboa.tecnico.meic.sec.util.Digest;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;
import pt.ulisboa.tecnico.meic.sec.util.VirtualCertificate;

import java.io.File;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public class IntentionToSellTampered extends Operation {

    public IntentionToSellTampered(ClientInterface ci, NotaryMiddleware ni) {
        super("IntentionToSellTampered", ci, ni);
    }

    @Override
    public boolean getAndCheckArgs() {
        try {
            args.add(Integer.parseInt(new BoxUI(REQUEST_GOODID).showAndGet()));
            args.add(Boolean.parseBoolean(new BoxUI(REQUEST_TOSELL).showAndGet()));

            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void execute() {
        Interaction response;

        int good = (int)args.get(0);
        boolean intention = (boolean)args.get(1);

        try {

            Certification cert = new VirtualCertificate();
            cert.init("", new File(System.getProperty("project.user.private.path") +
                    ClientService.userID + System.getProperty("project.user.private.ext")).getAbsolutePath());

            /*prepare request arguments*/
            Interaction request = new Interaction();
            request.setUserID(ClientService.userID);
            request.setGoodID(good);
            request.setResponse(intention);
            request.setUserClock(notaryInterface.getClock(ClientService.userID)+1);
            request.setHmac(Digest.createDigest(request, cert));

            request.setBuyerID(45);

            response = notaryInterface.intentionToSell(request);

            VirtualCertificate notaryCert = new VirtualCertificate();
            notaryCert.init(new File(System.getProperty("project.notary.cert.path")).getAbsolutePath());

            /*compare hmacs*/
            if(Digest.verify(response, notaryCert) == false){
                throw new HDSSecurityException(NOTARY_REPORT_TAMPERING);
            }

            /*check freshness*/
            if(request.getUserClock() != response.getUserClock()){
                throw new HDSSecurityException(NOTARY_REPORT_DUP_MSG);
            }

            setStatus(response.getResponse());
        }
        catch(GoodException e) {
            setStatus(Status.FAILURE_NOTARY_REPORT, e.getMessage());
        }
        catch (RemoteException e) {
            setStatus(Status.FAILURE_NOTARY_REPORT, e.getMessage());

        } catch (NoSuchAlgorithmException e) {
            setStatus(Status.FAILURE_DIGEST, e.getMessage());

        } catch (HDSSecurityException e) {
            setStatus(Status.FAILURE_SECURITY, e.getMessage());
        }

    }

    @Override
    public void visit(ClientVisitor visitor) {
        visitor.accept(this);
    }
}