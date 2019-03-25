package pt.ulisboa.tecnico.meic.sec.util;

import org.junit.Test;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;

import java.util.Random;

import static org.junit.Assert.*;
import static pt.ulisboa.tecnico.meic.sec.util.CCHelper.*;

public class CCHelperTest {

    /*@Test
    public void simplePKCS11_PTEID() throws HDSSecurityException {
        CCinit();
        CCstop();
    }

    @Test
    public void simpleSignTest() throws HDSSecurityException {
        CCinit();

        String data = "This is a test message";

        byte[] signature = CC_SignData(data.getBytes());

        assertEquals(true, CCverifySignature(signature));

        CCstop();
    }

    @Test
    public void simpleVerifyWrongSignature() throws HDSSecurityException {
        CCinit();

        byte[] array = new byte[256];
        new Random().nextBytes(array);

        assertEquals(false, CCverifySignature(array));

        CCstop();
    }*/
}