package pt.ulisboa.tecnico.meic.sec.util;

import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;

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

        String message1 = "Message test";
        String message2 = "Message test";

        byte[] signature1 = card.signData(message1.getBytes());
        byte[] signature2 = card.signData(message2.getBytes());

        assertEquals(false, card.verifyData(signature1, signature2));

        card.stop();
    }*/
}