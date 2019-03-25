package pt.ulisboa.tecnico.meic.sec.util;

import org.omg.CORBA.DynAnyPackage.Invalid;
import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;
import sun.security.pkcs11.wrapper.*;

import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Helper class for Cartão do Cidadão Operations.
 * NOTE: You should always call the CCInit first then the operations and lastly CCStop.
 *
 * CCInit();
 *  < ops >
 *    ...
 * CCStop();
 */
public class CCHelper {

    public static final String PTEID_LIB_NAME = "pteidlibj";

    /**
     * Gets the PKCS11 of the card
     * @return PKCS11 object
     */
    public static PKCS11 getCC_PKCS11() {

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
    public static byte[] CC_SignData(byte[] data) throws HDSSecurityException {

        //First get the CC_PKCS11
        PKCS11 pkcs11 = getCC_PKCS11();

        if (pkcs11 == null) {
            throw new HDSSecurityException("Invalid CC Card");
        }

        try {
            //Open the PKCS11 session
            System.out.println("            //Open the PKCS11 session");
            long p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);

            // Token login
            System.out.println("            //Token login");
            pkcs11.C_Login(p11_session, 1, null);
            CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(p11_session);

            // Get available keys
            System.out.println("            //Get available keys");
            CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
            attributes[0] = new CK_ATTRIBUTE();
            attributes[0].type = PKCS11Constants.CKA_CLASS;
            attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

            pkcs11.C_FindObjectsInit(p11_session, attributes);
            long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);

            // points to auth_key
            System.out.println("            //points to auth_key. No. of keys:" + keyHandles.length);

            /*
             * keyHandles[0] is the authenticity key (PT: chave de autenticação)
             * keyHandles[1] is the signature key (PT: chave de assinatura)
             * DO NOT USE signature key! --> Has legal value!
             */
            long signatureKey = keyHandles[0];
            pkcs11.C_FindObjectsFinal(p11_session);


            // initialize the signature method
            System.out.println("            //initialize the signature method");
            CK_MECHANISM mechanism = new CK_MECHANISM();
            mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
            mechanism.pParameter = null;
            pkcs11.C_SignInit(p11_session, mechanism, signatureKey);

            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

            // sign
            System.out.println("            //sign");
            byte[] signature = pkcs11.C_Sign(p11_session, data);
            System.out.println("            //signature:" + encoder.encode(signature));

            return signature;

        } catch (PKCS11Exception e) {
            throw new HDSSecurityException("SigningData problem: " + e.getMessage());
        }
    }

    /**
     * Given a signature data (return of CC_SignData) checks the validity of signature using the CC card
     * @param signature to be checked
     * @return true if signature is OK false otherwise
     */
    public static boolean CCverifySignature(byte[] signature) throws HDSSecurityException {
        PKCS11 pkcs11 = getCC_PKCS11();

        try {
            long p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);

            // Token login
            System.out.println("            //Token login");
            pkcs11.C_Login(p11_session, 1, null);

            PublicKey publicKey = getCitizenPublicKey();

            Signature verifySignature = Signature.getInstance("SHA1WithRSA");

            verifySignature.initVerify(publicKey);

            return verifySignature.verify(signature);

        } catch (PKCS11Exception e) {
            throw new HDSSecurityException("Insert your Citizen Card: " + e.getMessage());
        } catch (NoSuchAlgorithmException e){
            throw new HDSSecurityException("Wrong algorithm: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new HDSSecurityException("Invalid key exception: " + e.getMessage());
        } catch (SignatureException e) {
            throw new HDSSecurityException("Problem in Signature: " + e.getMessage());
        }
    }

    /**
     * Returns the n-th certificate, starting from 0
     * @param n
     * @return a binary byte array containing the certificate
     */
    private static byte[] getCertificateInBytes(int n) {
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
    public static X509Certificate getCitizenAuthCert() throws HDSSecurityException {

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

    /**
     * Returns the Citizen's Public Key
     * @return PublicKey object corresponding to the Citizen's Public Key
     * @throws HDSSecurityException if its not possible to obtain the Citizen Public Key from its Citizen Card
     */
    public static PublicKey getCitizenPublicKey() throws HDSSecurityException {
        X509Certificate citizenCertificate = getCitizenAuthCert();
        return citizenCertificate.getPublicKey();
    }

    /**
     * Starts CC operations
     */
    public static void CCinit() throws HDSSecurityException {
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
    public static void CCstop() throws HDSSecurityException {
        try {
            pteid.Exit(0);
        } catch (PteidException e) {
            throw new HDSSecurityException("Cannot stop CC");
        }
    }
}
