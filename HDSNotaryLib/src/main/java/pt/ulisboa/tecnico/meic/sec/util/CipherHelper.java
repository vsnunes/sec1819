package pt.ulisboa.tecnico.meic.sec.util;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;


public class CipherHelper {
    private static final String[] passphraseNotary = {"JjHiDqmTNF5Zq7", "vDrg8WqirRcQTQ", "cNiJI43Ihz7qun", "4ni857hguzbqDc", "ZFSUf8uYDHAHSJ"};
    private static final String[] passphraseUsers = {"wHNTULm6voEJE5", "a4U7SfXBB3NhrW", "wsf6KskMLJ9Z2h", "ZPWV2Rf7DkxBkt", "qprcPeqs9zTe76"};
    private static byte[] salt = {
            (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
            (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
    };

    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        Key secretKey = null;
        Cipher cipher = Cipher.getInstance("AES");

        File folder = new File("HDSNotaryLib/src/main/resources/certs/java_certs");
        File[] files = folder.listFiles();

        for(File file : files) {

            String fileName = file.getName();
            String entity = fileName.substring(fileName.indexOf('_') + 1, fileName.lastIndexOf('_'));
            String entityName = entity.substring(0, entity.length() - 1);
            int id = 0;
            if(!entity.equals("rootca"))
                id = Integer.parseInt(entity.substring(entity.length() - 1));

            if (entityName.equals("notary")) {
                KeySpec spec = new PBEKeySpec(passphraseNotary[id - 1].toCharArray(), salt, 65536, 256);
                SecretKey tmp = factory.generateSecret(spec);
                secretKey = new SecretKeySpec(tmp.getEncoded(),  "AES");
            }
            else if (entityName.equals("user")) {
                KeySpec spec = new PBEKeySpec(passphraseUsers[id - 1].toCharArray(), salt, 65536, 256);
                SecretKey tmp = factory.generateSecret(spec);
                secretKey = new SecretKeySpec(tmp.getEncoded(),  "AES");
            }
            cipher.init(new Integer(args[0]), secretKey);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            fileContent = cipher.doFinal(fileContent);

            FileOutputStream bos = new FileOutputStream(file);
            bos.write(fileContent);
            bos.close();

        }
    }
}
