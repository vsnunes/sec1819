package pt.ulisboa.tecnico.meic.sec.util;

import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static pt.ulisboa.tecnico.meic.sec.util.CertificateHelper.*;

/**
 * Allows to user virtual certification files to certificate user's
 */
public class VirtualCertificate implements Certification {

    /** Path to folder to user certificate **/
    private String pathToCertificate;

    /** Path to folder where private key is stored **/
    private String pathToPrivateKey;

    /** The user id **/
    private String id;

    private static final String[] passphraseNotary = {"JjHiDqmTNF5Zq7", "vDrg8WqirRcQTQ", "cNiJI43Ihz7qun", "4ni857hguzbqDc", "ZFSUf8uYDHAHSJ"};
    private static final String[] passphraseUsers = {"wHNTULm6voEJE5", "a4U7SfXBB3NhrW", "wsf6KskMLJ9Z2h", "ZPWV2Rf7DkxBkt", "qprcPeqs9zTe76"};

    @Override
    public void init(String... args) throws HDSSecurityException {
        int length = args.length;
        if (length > 0)
            this.pathToCertificate = args[0];
        if (length > 1)
            this.pathToPrivateKey = args[1];
        if (length > 2)
            this.id = args[2];
    }

    @Override
    public void stop() throws HDSSecurityException {

    }

    @Override
    public byte[] signData(byte[] data) throws HDSSecurityException {

        try {

            int indexOfSlash = pathToPrivateKey.lastIndexOf('/');
            if (indexOfSlash == -1) {
                indexOfSlash = pathToPrivateKey.lastIndexOf('\\');
            }

            String fileName = pathToPrivateKey.substring(indexOfSlash + 1);
            String entity = fileName.substring(fileName.indexOf('_') + 1, fileName.lastIndexOf('_'));
            String entityName = entity.substring(0, entity.length() - 1);
            int id = Integer.parseInt(entity.substring(entity.length() - 1));

            //NOTE: Just for easy project demos
            PrivateKey pvK = null;
            if (entityName.equals("notary")) {
                pvK = readPrivateKey(pathToPrivateKey, passphraseNotary[id - 1]);
            }
            else if (entityName.equals("user")) {
                pvK = readPrivateKey(pathToPrivateKey, passphraseUsers[id - 1]);
            }

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pvK);

            return cipher.doFinal(data);
        } catch(NoSuchAlgorithmException e) {
            throw new HDSSecurityException("No such algorithm: " + e.getMessage());
        } catch (IOException e) {
            throw new HDSSecurityException("IO Problem: " + e.getMessage());
      /*  } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new HDSSecurityException("Invalid Key: " + e.getMessage());*/
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new HDSSecurityException("Invalid Key exception: " + e.getMessage());
        /*} catch (URISyntaxException e) {
            throw new HDSSecurityException("URI problem: " + e.getMessage());*/
        } catch (NoSuchPaddingException e) {
            throw new HDSSecurityException("Padding problem when encrypting: " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            throw new HDSSecurityException("Illegal block when encrypting: " + e.getMessage());
        } catch (BadPaddingException e) {
            throw new HDSSecurityException("Bad padding problem: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyData(byte[] expected, byte[] original) throws HDSSecurityException {

        try {
            PublicKey puK = readPublicKey(pathToCertificate);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, puK);

            return Arrays.equals(expected, cipher.doFinal(original));

        } catch(NoSuchAlgorithmException e) {
            throw new HDSSecurityException("No such algorithm: " + e.getMessage());
        } catch(CertificateException e) {
            throw new HDSSecurityException("Public Key problem: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new HDSSecurityException("IO Problem: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new HDSSecurityException("Invalid Key exception: " + e.getMessage());
        } catch (NoSuchPaddingException e) {
            throw new HDSSecurityException("Padding problem when decrypting: " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            throw new HDSSecurityException("Illegal block when decrypting: " + e.getMessage());
        } catch (BadPaddingException e) {
            throw new HDSSecurityException("Bad padding problem: " + e.getMessage());
        }
    }
}
