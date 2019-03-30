package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.gui.BoxUI;
import pt.ulisboa.tecnico.meic.sec.interfaces.ClientInterface;
import pt.ulisboa.tecnico.meic.sec.interfaces.NotaryInterface;
import pt.ulisboa.tecnico.meic.sec.util.Digest;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;
import pt.ulisboa.tecnico.meic.sec.util.VirtualCertificate;

import java.io.File;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

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
    public void execute() {
        Interaction response;

        int good = (int)args.get(0);
        int buyer = (int)args.get(1);

        try {

            Interaction request = new Interaction();
            request.setGoodID(good);
            request.setBuyerID(buyer);
            request.setSellerID(ClientService.userID);

            VirtualCertificate cert = new VirtualCertificate();
            cert.init(new File("../HDSNotaryLib/src/main/resources/certs/user" + ClientService.userID + ".crt").getAbsolutePath(),
                    new File("../HDSNotaryLib/src/main/resources/certs/java_certs/private_user" + ClientService.userID + "_pkcs8.pem").getAbsolutePath());


            request.setHmac(Digest.createDigest(request, cert));

            response = notaryInterface.transferGood(request);

            VirtualCertificate notaryCert = new VirtualCertificate();
            notaryCert.init(new File("../HDSNotaryLib/src/main/resources/certs/rootca.crt").getAbsolutePath(),
                    new File("../HDSNotaryLib/src/main/resources/certs/java_certs/private_rootca_pkcs8.pem").getAbsolutePath());


            /*compare hmacs*/
            if(Digest.verify(response, notaryCert) == false){
                throw new HDSSecurityException(NOTARY_REPORT_TAMPERING);
            }

            /*check freshness*/
            if(request.getUserClock() != response.getUserClock()){
                throw new HDSSecurityException(NOTARY_REPORT_DUP_MSG);
            }

            setStatus(response.getResponse());

        } catch(TransactionException e) {
            setStatus(Status.FAILURE_TRANSACTION, e.getMessage());
        }
        catch (RemoteException e) {
            setStatus(Status.FAILURE_NOTARY_REPORT, e.getMessage());

        } catch (NoSuchAlgorithmException e) {
            setStatus(Status.FAILURE_DIGEST, e.getMessage());

        } catch (HDSSecurityException e) {
            setStatus(Status.FAILURE_SECURITY, e.getMessage());

        } catch (GoodException e) {
            setStatus(Status.FAILURE_GOOD, e.getMessage());
        }


    }

    @Override
    public void visit(ClientVisitor visitor) {
        visitor.accept(this);
    }
}
