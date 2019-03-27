package pt.ulisboa.tecnico.meic.sec.util;

import org.junit.Before;
import org.junit.Test;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;

import java.io.File;
import java.util.Random;

import static org.junit.Assert.*;

public class VirtualCertificateTest {

    private VirtualCertificate virtualSmartCard;

    @Before
    public void setUp() {
        virtualSmartCard = new VirtualCertificate();
    }

    @Test
    public void simpleSignTest() throws HDSSecurityException {
        virtualSmartCard.init(new File("src/main/resources/certs/user1.crt").getAbsolutePath(), new File("src/main/resources/certs/java_certs/private_user1_pkcs8.pem").getAbsolutePath());
        System.out.println(new File("src/main/resources/certs/user1.crt").getAbsolutePath());
        String data = "This is a test message";

        byte[] signature = virtualSmartCard.signData(data.getBytes());

        assertEquals(true, virtualSmartCard.verifySignature(signature));

        virtualSmartCard.stop();
    }

    @Test
    public void simpleVerifyWrongSignature() throws HDSSecurityException {
        virtualSmartCard.init(new File("src/main/resources/certs/user1.crt").getAbsolutePath(), new File("src/main/resources/certs/java_certs/private_user1_pkcs8.pem").getAbsolutePath());
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        assertEquals(false, virtualSmartCard.verifySignature(array));

        virtualSmartCard.stop();
    }
}