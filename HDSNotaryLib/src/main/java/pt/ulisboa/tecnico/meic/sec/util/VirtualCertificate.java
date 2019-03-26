package pt.ulisboa.tecnico.meic.sec.util;

import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import static pt.ulisboa.tecnico.meic.sec.util.CertificateHelper.*;

/**
 * Allows to user virtual certification files to certificate user's
 */
public class VirtualCertificate implements Certification {

    /** Path to folder to user certificate **/
    private String pathToCertificate;

    /** Path to folder where private key is stored **/
    private String pathToPrivateKey;

    /** The username **/
    private String userName;

    @Override
    public void init(String... args) throws HDSSecurityException {
        this.pathToCertificate = args[0];
        this.pathToPrivateKey = args[1];
    }

    @Override
    public void stop() throws HDSSecurityException {

    }

    @Override
    public byte[] signData(byte[] data) throws HDSSecurityException {

        try {
            Signature signature = Signature.getInstance("SHA1withRSA");

            PrivateKey pvK = readPrivateKey(pathToPrivateKey);

            signature.initSign(pvK);

            return signature.sign();
        } catch(NoSuchAlgorithmException e) {
            throw new HDSSecurityException("No such algorithm: " + e.getMessage());
        } catch (IOException e) {
            throw new HDSSecurityException("IO Problem: " + e.getMessage());
        } catch (InvalidKeySpecException e) {
            throw new HDSSecurityException("Invalid Key: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new HDSSecurityException("Invalid Key exception: " + e.getMessage());
        } catch (SignatureException e) {
            throw new HDSSecurityException("Signature problem: " + e.getMessage());
        } catch (URISyntaxException e) {
            throw new HDSSecurityException("URI problem: " + e.getMessage());
        }
    }

    @Override
    public boolean verifySignature(byte[] signature) throws HDSSecurityException {

        try {
            Signature verifySignature = Signature.getInstance("SHA1withRSA");

            PublicKey puK = readPublicKey(pathToCertificate);

            verifySignature.initVerify(puK);

            return verifySignature.verify(signature);
        } catch(NoSuchAlgorithmException e) {
            throw new HDSSecurityException("No such algorithm: " + e.getMessage());
        } catch(CertificateException e) {
            throw new HDSSecurityException("Public Key problem: " + e.getMessage());
        } catch (IOException e) {
            throw new HDSSecurityException("IO Problem: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new HDSSecurityException("Invalid Key exception: " + e.getMessage());
        } catch (SignatureException e) {
            throw new HDSSecurityException("Signature problem: " + e.getMessage());
        }
    }
}
