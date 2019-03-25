package pt.ulisboa.tecnico.meic.sec.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class NotaryResponse {

    private int buyerId;
    private int sellerId;
    private int goodId;
    private boolean response;
    private byte[] hmac;

    public NotaryResponse(int buyerId, int sellerId, int goodId, boolean response){
        Mac sha512_HMAC = null;
        String key = "chave";

        String result = buyerId + " " + sellerId + " " + goodId + " " + response;

        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.goodId = goodId;
        this.response = response;
        try{
            byte [] byteKey = key.getBytes(StandardCharsets.UTF_8);
            final String HMAC_SHA512 = "HmacSHA512";
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);

            sha512_HMAC = Mac.getInstance("HMAC_SHA512");
            sha512_HMAC.init(keySpec);

            hmac = sha512_HMAC.doFinal(result.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public byte[] calculateHMAC(){
        Mac sha512_HMAC;
        String key = "chave";

        String storedValues = buyerId + " " + sellerId + " " + goodId + " " + response;

        try{
            byte [] byteKey = key.getBytes(StandardCharsets.UTF_8);
            final String HMAC_SHA512 = "HmacSHA512";
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);

            sha512_HMAC = Mac.getInstance("HMAC_SHA512");
            sha512_HMAC.init(keySpec);

            return hmac = sha512_HMAC.doFinal(storedValues.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean validateResponse(){
        return hmac == calculateHMAC();
    }

}
