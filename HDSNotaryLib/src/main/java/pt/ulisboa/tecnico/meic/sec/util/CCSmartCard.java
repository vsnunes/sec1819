package pt.ulisboa.tecnico.meic.sec.util;

import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import sun.security.pkcs11.wrapper.*;

import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Certification operations for Cartão do Cidadão.
 * NOTE: You should always call the CCInit first then the operations and lastly CCStop.
 *
 * CCInit();
 *  < ops >
 *    ...
 * CCStop();
 */
public class CCSmartCard implements Certification {

    public static final String PTEID_LIB_NAME = "pteidlibj";

    /**
     * Gets the PKCS11 of the card
     * @return PKCS11 object
     */
    public PKCS11 getCC_PKCS11() {

        try {

            System.out.println("            //Load the PTEidlibj");

            PKCS11 pkcs11;
            String osName = System.getProperty("os.name");
            String javaVersion = System.getProperty("java.version");
            System.out.println("Java version: " + javaVersion);

            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

            String libName = "libpteidpkcs11.so";

            // access the ID and Address data via the pteidlib
            System.out.println("            -- accessing the ID  data via the pteidlib interface");

            // access the ID and Address data via the pteidlib
            System.out.println("            -- generating signature via the PKCS11 interface");


            if (-1 != osName.indexOf("Windows"))
                libName = "pteidpkcs11.dll";
            else if (-1 != osName.indexOf("Mac"))
                libName = "pteidpkcs11.dylib";
            Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
            if (javaVersion.startsWith("1.5.")) {
                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[]{String.class, CK_C_INITIALIZE_ARGS.class, boolean.class});
                pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[]{libName, null, false});
            } else {
                System.out.println("Before else invoke");
                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[]{String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class});
                pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[]{libName, "C_GetFunctionList", null, false});
            }
            return pkcs11;
        }

         catch (NoSuchMethodException e) {
            System.out.println("[Catch]  NoSuchMethodException:" + e.getMessage());
        }catch (IllegalAccessException e) {
            System.out.println("[Catch]  IllegalAccessException:" + e.getMessage());
        }catch (ClassNotFoundException e) {
            System.out.println("[Catch] ClassNotFoundException: " + e.getMessage());
        }catch (InvocationTargetException e) {
            System.out.println("[Catch] InvocationTargetException ");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Given an arbitrary binary data sign it using the public key
     * @param data to be signed
     * @return signature for the data
     * @throws HDSSecurityException if something fails (either the CC operation or the cryptographic ones)
     * Note: This function calls pteid API which will require PIN de Autenticação (Authentication PIN)
     */
    @Override
    public byte[] signData(byte[] data) throws HDSSecurityException {

        try {

            String f;
            if (System.getProperty("project.pteidlib.config") == null) {
                f = "src/main/resources/CitizenCard.cfg";
            } else {
                f = System.getProperty("project.pteidlib.config");
            }

            Provider p = new sun.security.pkcs11.SunPKCS11( f );
            Security.addProvider( p );


            Signature signature = Signature.getInstance("SHA1WithRSA");

            KeyStore ks = KeyStore.getInstance( "PKCS11", "SunPKCS11-PTeID" );
            /*
             * This is a special case of KeyStore where its contents are not copied into memory (and
             * this is why the parameters of the load method below are both null:
             */
            ks.load( null, null );

            // DEBUG! Prints all available keys. If you are reading this then the following code will help you :)

            /*Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                System.out.println( aliases.nextElement() );
            }*/

            //signature.initSign((PrivateKey) ks.getKey("CITIZEN AUTHENTICATION CERTIFICATE", null));

            PrivateKey pvK = (PrivateKey) ks.getKey("CITIZEN AUTHENTICATION CERTIFICATE", null);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pvK);


            return cipher.doFinal(data);

        } catch (NoSuchAlgorithmException e){
            throw new HDSSecurityException("Wrong algorithm: " + e.getMessage());
        } catch (NoSuchProviderException e) {
            throw new HDSSecurityException("Wrong provider: " + e.getMessage());
        } catch (KeyStoreException e) {
            throw new HDSSecurityException("Problem with keystore: " + e.getMessage());
        } catch (CertificateException e) {
            throw new HDSSecurityException("Problem with certificate: " + e.getMessage());
        } catch (IOException e) {
            throw new HDSSecurityException("IO Problem: " + e.getMessage());
        } catch (UnrecoverableKeyException e) {
            throw new HDSSecurityException("Unrecoverable key problem: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new HDSSecurityException("Invalid key exception: " + e.getMessage());
        } catch (NoSuchPaddingException e) {
            throw new HDSSecurityException("Padding problem when encrypting: " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            throw new HDSSecurityException("Illegal block when encrypting: " + e.getMessage());
        } catch (BadPaddingException e) {
            throw new HDSSecurityException("Bad padding problem: " + e.getMessage());
        }
    }

    /**
     * Given a signature data (return of CC_SignData) checks the validity of signature using the CC card
     * @param expected is the data which is expected to obtain
     * @param original is the original data received
     * @return true if digest is OK
     */
    @Override
    public boolean verifyData(byte[] expected, byte[] original) throws HDSSecurityException {

        try {

            PublicKey publicKey = getCitizenPublicKey();

            //Signature verifySignature = Signature.getInstance("SHA1WithRSA");

            //verifySignature.initVerify(publicKey);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            return Arrays.equals(expected, cipher.doFinal(original));


        } catch (NoSuchAlgorithmException e){
            throw new HDSSecurityException("Wrong algorithm: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new HDSSecurityException("Invalid key exception: " + e.getMessage());
        } catch (NoSuchPaddingException e) {
            throw new HDSSecurityException("Padding problem when decrypting: " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            throw new HDSSecurityException("Illegal block when decrypting: " + e.getMessage());
        } catch (BadPaddingException e) {
            throw new HDSSecurityException("Bad padding problem: " + e.getMessage());
        }
    }

    /**
     * Returns the n-th certificate, starting from 0
     * @param n
     * @return a binary byte array containing the certificate
     */
    private byte[] getCertificateInBytes(int n) {
        byte[] certificate_bytes = null;
        try {
            PTEID_Certif[] certs = pteid.GetCertificates();
            System.out.println("Number of certs found: " + certs.length);
            int i = 0;
            for (PTEID_Certif cert : certs) {
                System.out.println("-------------------------------\nCertificate #"+(i++));
                System.out.println(cert.certifLabel);
            }

            certificate_bytes = certs[n].certif; //gets the byte[] with the n-th certif

            //pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); // OBRIGATORIO Termina a eID Lib
        } catch (PteidException e) {
            e.printStackTrace();
        }
        return certificate_bytes;
    }

    /**
     * Returns the Notary Authentication Certificate in the X509 format
     * @return X509Certificate of the Citizen
     */
    public X509Certificate getCitizenAuthCert() throws HDSSecurityException {

        //0 certificate corresponds to the authentication certificate in the Citizen card!
        byte[] certificateEncoded = getCertificateInBytes(0);

        try {
            CertificateFactory f = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(certificateEncoded);
            X509Certificate cert = (X509Certificate) f.generateCertificate(in);

            return cert;

        } catch(CertificateException e) {
            throw new HDSSecurityException("Cannot get Citizen Cert: " + e.getMessage());
        }
    }

    public void writeCitizenAuthCertToFile(String pathFile) throws HDSSecurityException {
        try {
            X509Certificate cert = getCitizenAuthCert();
            String cert_begin = "-----BEGIN CERTIFICATE-----\n";
            String end_cert = "-----END CERTIFICATE-----";

            byte[] derCert = cert.getEncoded();
            String content = cert_begin + DatatypeConverter.printBase64Binary(derCert) + end_cert;

            BufferedWriter writer = new BufferedWriter(new FileWriter(pathFile));
            writer.write(content);

            writer.close();


        } catch (CertificateEncodingException e) {
            throw new HDSSecurityException("Failed to write cert to file: " + e.getMessage());
        } catch (FileNotFoundException e) {
            throw new HDSSecurityException("Failed to write cert to file (File Not Found): " + e.getMessage());
        } catch (IOException e) {
            throw new HDSSecurityException("Failed to write cert to file (IO Problem): " + e.getMessage());
        }
    }

    /**
     * Returns the Citizen's Public Key
     * @return PublicKey object corresponding to the Citizen's Public Key
     * @throws HDSSecurityException if its not possible to obtain the Citizen Public Key from its Citizen Card
     */
    public PublicKey getCitizenPublicKey() throws HDSSecurityException {
        X509Certificate citizenCertificate = getCitizenAuthCert();
        return citizenCertificate.getPublicKey();
    }

    /**
     * Starts CC operations
     */
    @Override
    public void init(String... args) throws HDSSecurityException {
        try {
            System.loadLibrary(PTEID_LIB_NAME);
            pteid.Init("");
            pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)
        }catch (PteidException e) {
            throw new HDSSecurityException("Cannot init CC, possible missing card");
        }
    }

    /**
     * Stops CC operations
     */
    @Override
    public void stop() throws HDSSecurityException {
        try {
            pteid.Exit(0);
        } catch (PteidException e) {
            throw new HDSSecurityException("Cannot stop CC");
        }
    }
}
