package pt.ulisboa.tecnico.meic.sec.util;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.xml.bind.DatatypeConverter;

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
}
