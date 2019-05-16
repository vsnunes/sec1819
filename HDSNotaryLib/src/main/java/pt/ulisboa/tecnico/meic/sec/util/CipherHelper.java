package pt.ulisboa.tecnico.meic.sec.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import static pt.ulisboa.tecnico.meic.sec.util.CertificateHelper.readPrivateKey;

public class CipherHelper {
    private static final String[] passphraseNotary = {"JjHiDqmTNF5Zq7", "vDrg8WqirRcQTQ", "cNiJI43Ihz7qun", "4ni857hguzbqDc", "ZFSUf8uYDHAHSJ"};
    private static final String[] passphraseUsers = {"wHNTULm6voEJE5", "a4U7SfXBB3NhrW", "wsf6KskMLJ9Z2h", "ZPWV2Rf7DkxBkt", "qprcPeqs9zTe76"};

    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
        Key secretKey = null;
        Cipher cipher = Cipher.getInstance("AES");

        File folder = new File("../../resources/certs/java_certs");
        File[] files = folder.listFiles();

        for(File file : files) {

            String fileName = file.getName();
            String entity = fileName.substring(fileName.indexOf('_') + 1, fileName.lastIndexOf('_'));
            String entityName = entity.substring(0, entity.length() - 1);
            int id = Integer.parseInt(entity.substring(entity.length() - 1));

            if (entityName.equals("notary")) {
                secretKey = new SecretKeySpec(passphraseNotary[id - 1].getBytes(),  "AES");
            }
            else if (entityName.equals("user")) {
                secretKey = new SecretKeySpec(passphraseUsers[id - 1].getBytes(),  "AES");
            }
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            fileContent = cipher.doFinal(fileContent);

            FileOutputStream bos = new FileOutputStream(file);
            bos.write(fileContent);
            bos.close();

        }
    }
}
