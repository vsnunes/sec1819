package pt.ulisboa.tecnico.meic.sec.util;

import org.junit.Test;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;

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
    }*/
}