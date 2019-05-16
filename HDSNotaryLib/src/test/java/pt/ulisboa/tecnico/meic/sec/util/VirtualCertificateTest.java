package pt.ulisboa.tecnico.meic.sec.util;

import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;

import java.io.File;
import java.util.Random;

import static org.junit.Assert.*;
import static pt.ulisboa.tecnico.meic.sec.util.KeysHelper.createKey;

public class VirtualCertificateTest {

    public static final String USER_1_CRT = "src/main/resources/certs/user1.crt";
    public static final String USER_1_PKCS_8_PEM = "src/main/resources/certs/java_certs/private_user1_pkcs8.pem";

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
    public void simpleSignTest() throws Exception {
        virtualSmartCard.init(new File(USER_1_CRT).getAbsolutePath(), new File(USER_1_PKCS_8_PEM).getAbsolutePath());
        System.out.println(new File("src/main/resources/certs/user1.crt").getAbsolutePath());
        String data = "This is a test message";

        byte[] signature = virtualSmartCard.signData(data.getBytes(StandardCharsets.UTF_8));

        //assertEquals(true, virtualSmartCard.verifyData(data.getBytes(), signature));

        virtualSmartCard.stop();
    }

    /**
     * Tests for different signatures
     * @throws HDSSecurityException
     */
    @Test
    public void simpleVerifyWrongSignature() throws HDSSecurityException {
        virtualSmartCard.init(new File(USER_1_CRT).getAbsolutePath(), new File(USER_1_PKCS_8_PEM).getAbsolutePath());
        byte[] array1 = new byte[128];
        new Random().nextBytes(array1);
        byte[] array2 = new byte[128];
        new Random().nextBytes(array2);

        byte[] signature1 = virtualSmartCard.signData(array1);
        byte[] signature2 = virtualSmartCard.signData(array2);

        assertEquals(false, virtualSmartCard.verifyData(signature1, signature2));

        virtualSmartCard.stop();
    }
}