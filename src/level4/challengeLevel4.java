package level4;

import shared.utils.messages.MessageHelper;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static shared.enumerations.CmdColors.GREEN;

public class challengeLevel4 {

    private final String URL = "itech-app.agreeableforest-7d80e3cc.westeurope.azurecontainerapps.io";
    private final int PORT = 1337;

    // Declare a variable to store the private key
    private String privateKeyPem = "";

    public static void main(String[] args) throws Exception {
        new challengeLevel4().run();
    }

    public void run() throws Exception {
        try (Socket server = new Socket(URL, PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream(), StandardCharsets.UTF_8));
             OutputStream output = server.getOutputStream()) {

            // Continuously read messages from the server
            String message;
            StringBuilder privateKeyBuilder = new StringBuilder();
            boolean isPrivateKeySection = false;
            boolean bytesSent = false; // track initial bytes were sent
            boolean studentNumberSent = false; // track student number was sent
            boolean octalBytesSent = false; // track octal bytes were sent
            boolean base64Decoded = false; // track base64 data was decoded and sent
            boolean aesDecrypted = false; // track AES data was decrypted and sent
            boolean rsaDecrypted = false; // track RSA data was decrypted and sent

            while ((message = reader.readLine()) != null) {
                //message contains the private key section
                if (message.contains("-----BEGIN PRIVATE KEY-----")) {
                    isPrivateKeySection = true;
                }
                if (isPrivateKeySection) {
                    privateKeyBuilder.append(message).append("\n");
                    //end of the private key section
                    if (message.contains("-----END PRIVATE KEY-----")) {
                        isPrivateKeySection = false;
                        privateKeyPem = privateKeyBuilder.toString(); // store private key
                        privateKeyBuilder.setLength(0);
                        System.out.println("Private key stored securely.");
                        continue;
                    }
                } else {
                    MessageHelper.printColoredMessage(GREEN, message);
                }

                if (!bytesSent && message.contains("In order to continue this challenge you have to send the following bytes")) {
                    byte[] bytesToSend = new byte[]{(byte) 0x50, (byte) 0x39, (byte) 0xa1};
                    output.write(bytesToSend);
                    output.flush();
                    System.out.println("Bytes sent to server: 0x50, 0x39, 0xa1");
                    bytesSent = true;
                }

                if (bytesSent && !studentNumberSent && message.contains("We need your student number to continue")) {
                    byte[] studentNumberBytes = new byte[]{(byte) '5', (byte) '5', (byte) '4', (byte) '5', (byte) '1', (byte) '4'};
                    output.write(studentNumberBytes);
                    output.flush();
                    System.out.println("Student number sent to server: 554514");
                    studentNumberSent = true;
                }

                if (studentNumberSent && !octalBytesSent && message.contains("Here are three bytes in octal representation")) {
                    byte[] octalBytes = new byte[]{
                            (byte) 0103,
                            (byte) 0247,
                            (byte) 0231
                    };
                    output.write(octalBytes);
                    output.flush();
                    System.out.println("Octal bytes sent to server: 103, 247, 231");
                    octalBytesSent = true;
                }

                if (!base64Decoded && message.contains("The following data is encoded using base64 encoding")) {
                    String base64EncodedData = message.substring(message.indexOf(":") + 1).trim();

                    byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedData);

                    output.write(decodedBytes);
                    output.flush();

                    System.out.println("Decoded Base64 data sent to server.");
                    base64Decoded = true;
                }

                if (!aesDecrypted && message.contains("AES-128 in ECB mode")) {
                    String base64Key = "cvVtQ55FZZ2c/wEFFe7KSg==";
                    String base64Ciphertext = "aV1QyC2KP8Ir1hxDUJEfdpSwR3LLsQxfD86zIKlecDidaPz0R/gBNiSSTkYufiM4cr/RGxC0/uJBDS1+oo/36hLjl/HswMbabZKD6CK02Ag=";

                    byte[] secretKey = Base64.getDecoder().decode(base64Key);
                    byte[] ciphertext = Base64.getDecoder().decode(base64Ciphertext);

                    byte[] decryptedMessage = decryptAES(secretKey, ciphertext);

                    output.write(decryptedMessage);
                    output.flush();

                    System.out.println("Decrypted message sent to server.");
                    aesDecrypted = true;
                }

                if (!rsaDecrypted && message.contains("RSA/ECB/PKCS1Padding")) {
                    try {
                        String base64Ciphertext = message.substring(message.lastIndexOf(":") + 1).trim();

                        base64Ciphertext = base64Ciphertext.replaceAll("[^A-Za-z0-9+/=]", "");
                        while (base64Ciphertext.length() % 4 != 0) {
                            base64Ciphertext += "=";
                        }

                        byte[] ciphertext = Base64.getDecoder().decode(base64Ciphertext);

                        byte[] decryptedMessage = decryptRSA(privateKeyPem, ciphertext);

                        String decryptedText = new String(decryptedMessage, StandardCharsets.UTF_8);
                        System.out.println("Decrypted RSA Message: " + decryptedText);

                        output.write(decryptedMessage);
                        output.flush();

                        rsaDecrypted = true;
                    } catch (IllegalArgumentException e) {
                        System.err.println("Failed to decode Base64 string: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("An error occurred during RSA decryption: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] decryptAES(byte[] secretKey, byte[] ciphertext) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(secretKey, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        return cipher.doFinal(ciphertext);
    }

    private byte[] decryptRSA(String privateKeyPem, byte[] ciphertext) throws Exception {
        String privateKeyPEM = privateKeyPem.replace("-----BEGIN PRIVATE KEY-----\n", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\n", "");

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(ciphertext);
    }
}