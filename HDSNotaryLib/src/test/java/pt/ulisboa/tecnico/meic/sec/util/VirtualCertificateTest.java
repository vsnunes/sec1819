package pt.ulisboa.tecnico.meic.sec.util;

import java.nio.charset.StandardCharsets;
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

    /**
     * Test for Small message signature success
     * @throws HDSSecurityException
     */
    @Test
    public void simpleSignTest() throws HDSSecurityException {
        virtualSmartCard.init(new File("src/main/resources/certs/user1.crt").getAbsolutePath(), new File("src/main/resources/certs/java_certs/private_user1_pkcs8.pem").getAbsolutePath());
        System.out.println(new File("src/main/resources/certs/user1.crt").getAbsolutePath());
        String data = "This is a test message";

        byte[] signature = virtualSmartCard.signData(data.getBytes(StandardCharsets.UTF_8));

        assertEquals(true, virtualSmartCard.verifySignature(data.getBytes(), signature));

        virtualSmartCard.stop();
    }

    /**
     * Tests for different signatures
     * @throws HDSSecurityException
     */
    @Test
    public void simpleVerifyWrongSignature() throws HDSSecurityException {
        virtualSmartCard.init(new File("src/main/resources/certs/user1.crt").getAbsolutePath(), new File("src/main/resources/certs/java_certs/private_user1_pkcs8.pem").getAbsolutePath());
        byte[] array1 = new byte[128];
        new Random().nextBytes(array1);
        byte[] array2 = new byte[128];
        new Random().nextBytes(array2);

        byte[] signature1 = virtualSmartCard.signData(array1);
        byte[] signature2 = virtualSmartCard.signData(array2);

        assertEquals(false, virtualSmartCard.verifySignature(signature1, signature2));

        virtualSmartCard.stop();
    }
}