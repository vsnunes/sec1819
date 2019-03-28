package pt.ulisboa.tecnico.meic.sec.util;

import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digest {
    public static byte[] createDigest(Interaction data, Certification cert) throws NoSuchAlgorithmException, HDSSecurityException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return cert.signData(digest.digest(data.toString().getBytes(StandardCharsets.UTF_8)));
    }
    public static boolean verify(Interaction data, Certification cert) throws NoSuchAlgorithmException, HDSSecurityException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] received = digest.digest(data.toString().getBytes(StandardCharsets.UTF_8));

        return cert.verifyData(data.getHmac(), received);

    }
}
