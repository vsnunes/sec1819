package pt.ulisboa.tecnico.meic.sec.util;

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

        byte[] signature = card.signData(data.getBytes());

        assertEquals(true, card.verifySignature(signature));

        card.stop();
    }

    @Test
    public void simpleVerifyWrongSignature() throws HDSSecurityException {
        card.init();

        byte[] array = new byte[256];
        new Random().nextBytes(array);

        assertEquals(false, card.verifySignature(array));

        card.stop();
    }*/
}