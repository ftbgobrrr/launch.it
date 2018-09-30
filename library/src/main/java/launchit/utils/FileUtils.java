package launchit.utils;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

    public static boolean verifyChecksum(File file, String testChecksum)
    {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            FileInputStream fis = new FileInputStream(file);

            byte[] data = new byte[65536];
            int read;
            while ((read = fis.read(data)) != -1) {
                sha1.update(data, 0, read);
            }
            byte[] hashBytes = sha1.digest();

            StringBuilder sb = new StringBuilder();
            for (byte hashByte : hashBytes) {
                sb.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }

            String fileHash = sb.toString();

            return fileHash.equals(testChecksum);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String urlChecksum(URL url)
    {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            InputStream fis = url.openStream();

            byte[] data = new byte[1024];
            int read;
            while ((read = fis.read(data)) != -1) {
                sha1.update(data, 0, read);
            }
            fis.close();
            byte[] hashBytes = sha1.digest();
            sha1.reset();
            BigInteger bigInt = new BigInteger(1, hashBytes);
            return String.format("%0" + (hashBytes.length << 1) + "x", bigInt);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getInstallDir(String dotName) {
        String userHome = System.getProperty("user.home", ".");
        File workingDirectory;
        switch (OperatingSystem.getCurrentPlatform().ordinal()) {
            case 0:
                workingDirectory = new File(userHome, dotName);
                break;
            case 1:
                String applicationData = System.getenv("APPDATA");
                String folder = applicationData != null ? applicationData : userHome;

                workingDirectory = new File(folder, dotName);
                break;
            case 2:
                workingDirectory = new File(userHome, String.format("Library/Application Support/%s", dotName));
                break;
            default:
                workingDirectory = new File(userHome, dotName);
        }
        return workingDirectory;
    }

}
