package pt.ulisboa.tecnico.meic.sec.util;

import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;

import java.util.Random;

public class CCSmartCardTest {

    private CCSmartCard card;

    /*@Before
    public void setUp() {
        card = new CCSmartCard();
    }

    @Test
    public void simplePKCS11_PTEID() throws HDSSecurityException {
        card.init();
        card.stop();
    }

    @Test
    public void simpleSignTest() throws HDSSecurityException {
        card.init();

        String data = "This is a test message";

        byte[] signature = card.signData(data.getBytes(StandardCharsets.UTF_8));

        assertEquals(true, card.verifyData(data.getBytes(), signature));

        card.stop();
    }

    @Test
    public void simpleVerifyWrongSignature() throws HDSSecurityException {
        card.init();

        byte[] array1 = new byte[128];
        new Random().nextBytes(array1);
        byte[] array2 = new byte[128];
        new Random().nextBytes(array2);

        byte[] signature1 = card.signData(array1);
        byte[] signature2 = card.signData(array2);

        assertEquals(false, card.verifyData(signature1, signature2));

        card.stop();
    }*/
}