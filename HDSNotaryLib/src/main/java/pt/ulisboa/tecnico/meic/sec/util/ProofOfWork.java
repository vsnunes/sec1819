package pt.ulisboa.tecnico.meic.sec.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ProofOfWork {

    private int _nounce;
    private byte[] _hash;

    private ProofOfWork(int nounce, byte[] hash) {
        this._nounce = nounce;
        this._hash = hash;
    }

    public int getNounce() {
        return _nounce;
    }

    public byte[] getHash() {
        return _hash;
    }

    public static ProofOfWork calculateHMAC(String key, String content, int difficulty){
        int nounce = 0;
        Mac sha512_HMAC;

        byte[] hmac;
        String storedValues = content + nounce;
        try{
            byte [] byteKey = key.getBytes(StandardCharsets.UTF_8);
            final String HMAC_SHA512 = "HmacSHA512";
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);

            sha512_HMAC = Mac.getInstance(HMAC_SHA512);
            sha512_HMAC.init(keySpec);

            hmac = sha512_HMAC.doFinal(storedValues.getBytes(StandardCharsets.UTF_8));

            while(!verifyProof(hmac, difficulty)) {
                nounce++;
                storedValues = content + nounce;
                hmac = sha512_HMAC.doFinal(storedValues.getBytes(StandardCharsets.UTF_8));
            }

            return new ProofOfWork(nounce, hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean verifyProof(byte[] hmac, int difficulty) {
        int i = 0;
        while (i < difficulty) {
            if(hmac[i] != 0)
                return false;
            i++;
        }
        return true;
    }

    public static byte[] calculateWithNounce(String key, String content, int nounce) {
        Mac sha512_HMAC;

        byte[] hmac;
        String storedValues = content + nounce;
        try{
            byte [] byteKey = key.getBytes(StandardCharsets.UTF_8);
            final String HMAC_SHA512 = "HmacSHA512";
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);

            sha512_HMAC = Mac.getInstance(HMAC_SHA512);
            sha512_HMAC.init(keySpec);

            hmac = sha512_HMAC.doFinal(storedValues.getBytes(StandardCharsets.UTF_8));

            return hmac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }
}
