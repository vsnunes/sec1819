package pt.ulisboa.tecnico.meic.sec.util;

import java.io.*;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;

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
}
