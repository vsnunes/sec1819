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
    public void execute() {
        Interaction response;

        int good = (int)args.get(0);

        try {
            Interaction request = new Interaction();
            request.setUserID(ClientService.userID);
            request.setGoodID(good);

            VirtualCertificate cert = new VirtualCertificate();
            cert.init(new File("../HDSNotaryLib/src/main/resources/certs/rootca.crt").getAbsolutePath(),
                    new File("../HDSNotaryLib/src/main/resources/certs/java_certs/private_rootca_pkcs8.pem").getAbsolutePath());


            request.setHmac(Digest.createDigest(request, cert));

            response = notaryInterface.getStateOfGood(request);


            /*compare hmacs*/
            if(Digest.verify(response, cert) == false){
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
