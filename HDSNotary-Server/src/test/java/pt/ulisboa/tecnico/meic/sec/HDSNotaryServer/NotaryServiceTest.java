package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pt.ulisboa.tecnico.meic.sec.exceptions.GoodException;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import pt.ulisboa.tecnico.meic.sec.exceptions.TransactionException;
import pt.ulisboa.tecnico.meic.sec.util.Digest;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;
import pt.ulisboa.tecnico.meic.sec.util.VirtualCertificate;

import java.io.File;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

public class NotaryServiceTest {

    private Interaction request;
    private NotaryService notary;

    public NotaryServiceTest() throws RemoteException, GoodException {
        System.setProperty("project.users.cert.path","../HDSNotaryLib/src/main/resources/certs/user");
        System.setProperty("project.users.cert.ext", ".crt");
        System.setProperty("project.notary.private","../HDSNotaryLib/src/main/resources/certs/java_certs/private_rootca_pkcs8.pem");
        System.setProperty("project.user.private.path", "../HDSNotaryLib/src/main/resources/certs/java_certs/private_user");
        System.setProperty("project.user.private.ext", "_pkcs8.pem");
        System.setProperty("project.nameserver.config", "../HDSNotaryLib/src/main/resources/Servers.cfg");
    }

    @Before
    public  void setUp() throws RemoteException, GoodException {
        request = new Interaction();
        NotaryService.setForTest(true); //do not init real connections to servers
        notary = NotaryService.getInstance();
        notary.createUser();
        notary.createGood();
    }

    @Test
    public void intentionToSellTestTrue() throws GoodException, RemoteException, HDSSecurityException, NoSuchAlgorithmException {

        VirtualCertificate cert = new VirtualCertificate();
        cert.init("", new File(System.getProperty("project.user.private.path") +
                1 + System.getProperty("project.user.private.ext")).getAbsolutePath());

        request.setUserID(1);
        request.setGoodID(1);
        request.setResponse(true);
        request.setUserClock(1);
        request.setHmac(Digest.createDigest(request, cert));
        Interaction reply = notary.intentionToSell(request);
        Assert.assertEquals(reply.getGoodID(), 1);
        Assert.assertEquals(reply.getUserID(), 1);
        Assert.assertEquals(reply.getUserClock(),1);
        Assert.assertTrue(reply.getResponse());
    }

    @Test
    public void intentionToSellTestFalse() throws GoodException, RemoteException, HDSSecurityException, NoSuchAlgorithmException {

        VirtualCertificate cert = new VirtualCertificate();
        cert.init("", new File(System.getProperty("project.user.private.path") +
                1 + System.getProperty("project.user.private.ext")).getAbsolutePath());

        request.setUserID(1);
        request.setGoodID(1);
        request.setResponse(false);
        request.setUserClock(1);
        request.setHmac(Digest.createDigest(request, cert));
        Interaction reply = notary.intentionToSell(request);
        Assert.assertEquals(reply.getGoodID(), 1);
        Assert.assertEquals(reply.getUserID(), 1);
        Assert.assertEquals(reply.getUserClock(),1);
        Assert.assertFalse(reply.getResponse());
    }

    @Test
    public void getStateOfGoodTest() throws GoodException, RemoteException, HDSSecurityException, NoSuchAlgorithmException {

        VirtualCertificate cert = new VirtualCertificate();
        cert.init("", new File(System.getProperty("project.user.private.path") +
                1 + System.getProperty("project.user.private.ext")).getAbsolutePath());

        request.setUserID(1);
        request.setGoodID(1);
        request.setResponse(false);
        request.setUserClock(1);
        request.setHmac(Digest.createDigest(request, cert));
        Interaction reply = notary.getStateOfGood(request);
        Assert.assertEquals(reply.getGoodID(), 1);
        Assert.assertEquals(reply.getUserID(), 1);
        Assert.assertEquals(reply.getUserClock(),1);
        Assert.assertFalse(reply.getResponse());
    }

    @Test
    public void transferGoodTest() throws HDSSecurityException, NoSuchAlgorithmException, RemoteException, GoodException, TransactionException {
        VirtualCertificate cert = new VirtualCertificate();
        cert.init("", new File(System.getProperty("project.user.private.path") +
                1 + System.getProperty("project.user.private.ext")).getAbsolutePath());

        request.setUserID(1);
        request.setGoodID(1);
        request.setResponse(true);
        request.setUserClock(1);
        request.setHmac(Digest.createDigest(request, cert));
        notary.intentionToSell(request);

        Interaction buyerRequest  = new Interaction();
        buyerRequest.setBuyerID(1);
        buyerRequest.setGoodID(1);
        buyerRequest.setSellerID(1);
        buyerRequest.setBuyerClock(2);
        String data = "" + 1 + 1 + 2 + 2;
        buyerRequest.setBuyerHMAC(Digest.createDigest(data, cert));

        buyerRequest.setBuyerClock(2);
        buyerRequest.setSellerClock(2);
        data = "" + 1 + 1 + 1 + 2 + 2;
        buyerRequest.setSellerHMAC(Digest.createDigest(data, cert));
        notary.transferGood(buyerRequest);
    }

    @Test(expected = HDSSecurityException.class)
    public void replayAttackIntentionToSell() throws GoodException, RemoteException, HDSSecurityException, NoSuchAlgorithmException {
        VirtualCertificate cert = new VirtualCertificate();
        cert.init("", new File(System.getProperty("project.user.private.path") +
                1 + System.getProperty("project.user.private.ext")).getAbsolutePath());

        request.setUserID(1);
        request.setGoodID(1);
        request.setResponse(true);
        request.setUserClock(1);
        request.setHmac(Digest.createDigest(request, cert));

        Interaction replayAttack = new Interaction();
        replayAttack.setUserID(1);
        replayAttack.setGoodID(1);
        replayAttack.setResponse(true);
        replayAttack.setUserClock(1);
        replayAttack.setHmac(Digest.createDigest(request, cert));
        notary.intentionToSell(request);
        notary.intentionToSell(replayAttack);
     }

    @Test(expected = HDSSecurityException.class)
    public void replayAttackTransferGoods() throws GoodException, RemoteException, HDSSecurityException, NoSuchAlgorithmException, TransactionException {
        VirtualCertificate cert = new VirtualCertificate();
        cert.init("", new File(System.getProperty("project.user.private.path") +
                1 + System.getProperty("project.user.private.ext")).getAbsolutePath());

        request.setUserID(1);
        request.setGoodID(1);
        request.setResponse(true);
        request.setUserClock(1);
        request.setHmac(Digest.createDigest(request, cert));
        notary.intentionToSell(request);

        Interaction buyerRequest  = new Interaction();
        buyerRequest.setBuyerID(1);
        buyerRequest.setGoodID(1);
        buyerRequest.setSellerID(1);
        buyerRequest.setBuyerClock(2);
        String data = "" + 1 + 1 + 2 + 2;
        buyerRequest.setBuyerHMAC(Digest.createDigest(data, cert));

        buyerRequest.setBuyerClock(2);
        buyerRequest.setSellerClock(2);
        data = "" + 1 + 1 + 1 + 2 + 2;
        buyerRequest.setSellerHMAC(Digest.createDigest(data, cert));

        Interaction secondRequest = new Interaction();
        secondRequest.setBuyerID(1);
        secondRequest.setGoodID(1);
        secondRequest.setSellerID(1);
        secondRequest.setBuyerClock(2);
        data = "" + 1 + 1 + 2 + 2;
        secondRequest.setBuyerHMAC(Digest.createDigest(data, cert));

        secondRequest.setBuyerClock(2);
        secondRequest.setSellerClock(2);
        data = "" + 1 + 1 + 1 + 2 + 2;
        secondRequest.setSellerHMAC(Digest.createDigest(data, cert));

        notary.transferGood(buyerRequest);
        notary.transferGood(secondRequest);

    }

    @After
    public void tearDown() {
        notary.reset();
    }

}