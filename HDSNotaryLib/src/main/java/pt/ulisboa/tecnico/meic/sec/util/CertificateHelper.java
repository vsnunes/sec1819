package pt.ulisboa.tecnico.meic.sec.util;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;

/**
 * A class for describing basic RSA certificates operations.
 */
public class CertificateHelper {

    private static byte[] salt = {
            (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
            (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
    };

    /**
     * Reads a public key from certificate
     * @param certificatePath the certificate file name
     * @return PublicKey object
     * @throws FileNotFoundException
     * @throws CertificateException
     */
    public static PublicKey readPublicKey(String certificatePath) throws CertificateException, IOException {

        //Read the certificate from file
        FileInputStream fis = new FileInputStream (certificatePath);
        BufferedInputStream bis = new BufferedInputStream (fis);
        CertificateFactory cf = CertificateFactory.getInstance ("X.509");
        if (bis.available () == 0) {
            System.exit (0);
        }

        //Parse binary to Certificate class
        Certificate cert = cf.generateCertificate (bis);

        //Extract Cert Public Key
        PublicKey pub = cert.getPublicKey ();
        return pub;
    }

    /**
     * Reads a private key from file
     * @param privateKeyPath the filename of the PEM file in certificates directory
     * @return PrivateKey object to be used to encrypt/decrypt data.
     * @throws IOException
     */
    public static PrivateKey readPrivateKey(String privateKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, URISyntaxException {

        String privateKeyContent = new String(Files.readAllBytes(Paths.get(privateKeyPath)), StandardCharsets.UTF_8);

        privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");

        KeyFactory kf = KeyFactory.getInstance("RSA");

        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(privateKeyContent));
        PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
        return privKey;

    }

    /**
     * Reads a private key from file
     * @param privateKeyPath the filename of the PEM file in certificates directory
     * @param passphrase the passphrase
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws URISyntaxException
     */
    public static PrivateKey readPrivateKey(String privateKeyPath, String passphrase) throws IOException {
        try {

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            Key secretKey = null;
            Cipher cipher = Cipher.getInstance("AES");

            File file = new File(privateKeyPath);
            KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            secretKey = new SecretKeySpec(tmp.getEncoded(),  "AES");

            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            fileContent = cipher.doFinal(fileContent);

            String privateKeyContent = new String(fileContent);

            privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");

            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(privateKeyContent));
            PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
            return privKey;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    return null;
    }
}
