package pt.ulisboa.tecnico.meic.sec.util;

import org.bouncycastle.util.encoders.Base64;

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
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.file.Path;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.KeySpec;
/*import java.util.Base64;
import java.util.Base64.Decoder;*/
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * A class for describing basic RSA certificates operations.
 */
public class CertificateHelper {

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
    public static PrivateKey readPrivateKey(String privateKeyPath, String passphrase) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, URISyntaxException {

        //String privateKeyContent = new String(Files.readAllBytes(Paths.get(privateKeyPath)), StandardCharsets.UTF_8);

        byte[] file = Files.readAllBytes(Paths.get(privateKeyPath));


        try {
            /*privateKeyContent = privateKeyContent.replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "");
            privateKeyContent = privateKeyContent.replace("-----END ENCRYPTED PRIVATE KEY-----", "");
            privateKeyContent = privateKeyContent.replaceAll("\\n", "");*/
            EncryptedPrivateKeyInfo pkInfo = new EncryptedPrivateKeyInfo(file);
            PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray());
            SecretKeyFactory pbeKeyFactory = SecretKeyFactory.getInstance(pkInfo.getAlgName());
            PKCS8EncodedKeySpec encodedKeySpec = pkInfo.getKeySpec(pbeKeyFactory.generateSecret(keySpec));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(encodedKeySpec);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("TIAGO E sfsdfs " + e.getMessage());
            e.printStackTrace();
        }

        return null;
        //return fromPem(Paths.get(privateKeyPath), passphrase);

    }

    /**
     * Retrieve {@link PrivateKey} from PEM encoded PKCS#8 file.
     *
     * @param path path to the PKCS#8 PEM file
     * @param password null if not encrypted
     * @return
     */
    /*public static PrivateKey fromPem(Path path, String password) {
        PrivateKey privateKey;
        try {
            byte[] der = getDer(path);
            KeySpec keySpec = getKeySpec(der, password);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(keySpec);
        } catch (Exception e) {
            String msg = String.format("Failed to read PKCS#8 PEM file. file=%s", path);
            throw new RuntimeException(msg, e);
        }
        return privateKey;
    }*/

    /**
     * Convert PKCS#8 PEM file to DER encoded private key
     */
    /*private static byte[] getDer(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);

        // remove header and footer, combine to single string without line breaks
        String base64Text = lines.subList(1, lines.size() - 1).stream().collect(joining());

        Decoder decoder = Base64.getDecoder();
        return decoder.decode(base64Text);
    }*/

    /*private static KeySpec getKeySpec(byte[] encodedKey, String password) throws Exception {
        KeySpec keySpec;
        if (password == null) {
            keySpec = new PKCS8EncodedKeySpec(encodedKey);
        } else {
            // decrypt private key
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());

            EncryptedPrivateKeyInfo privateKeyInfo = new EncryptedPrivateKeyInfo(encodedKey);
            String algorithmName = privateKeyInfo.getAlgName();
            Cipher cipher = Cipher.getInstance(algorithmName);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithmName);

            Key pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);
            AlgorithmParameters algParams = privateKeyInfo.getAlgParameters();
            cipher.init(Cipher.DECRYPT_MODE, pbeKey, algParams);
            keySpec = privateKeyInfo.getKeySpec(cipher);
        }
        return keySpec;
    }*/
}
