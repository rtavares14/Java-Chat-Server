package level4;

import shared.utils.messages.MessageHelper;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import static shared.enumerations.CmdColors.*;

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
            boolean bytesSent = false; // Flag to track if the initial bytes were sent
            boolean studentNumberSent = false; // Flag to track if the student number was sent
            boolean octalBytesSent = false; // Flag to track if the octal bytes were sent
            boolean base64Decoded = false; // Flag to track if the base64 data was decoded and sent
            boolean aesDecrypted = false;
            boolean rsaDecrypted = false;

            while ((message = reader.readLine()) != null) {
                // Check if the message contains the private key section
                if (message.contains("-----BEGIN PRIVATE KEY-----")) {
                    isPrivateKeySection = true;
                }
                if (isPrivateKeySection) {
                    privateKeyBuilder.append(message).append("\n");
                    // Check for the end of the private key section
                    if (message.contains("-----END PRIVATE KEY-----")) {
                        isPrivateKeySection = false;
                        privateKeyPem = privateKeyBuilder.toString(); // Store the private key
                        privateKeyBuilder.setLength(0); // Reset the StringBuilder
                        System.out.println("Private key stored securely.");
                        continue; // Skip printing the private key message
                    }
                } else {
                    // Print other server messages
                    MessageHelper.printColoredMessage(GREEN, "Message from server:" + message);
                }

                // Send the required bytes if instructed
                if (!bytesSent && message.contains("In order to continue this challenge you have to send the following bytes")) {
                    byte[] bytesToSend = new byte[]{(byte) 0x50, (byte) 0x39, (byte) 0xa1};
                    output.write(bytesToSend);
                    output.flush();
                    System.out.println("Bytes sent to server: 0x50, 0x39, 0xa1");
                    bytesSent = true; // Mark the bytes as sent
                }

                // Send the student number when requested
                if (bytesSent && !studentNumberSent && message.contains("We need your student number to continue")) {
                    byte[] studentNumberBytes = new byte[]{(byte) '5', (byte) '5', (byte) '4', (byte) '5', (byte) '1', (byte) '4'};
                    output.write(studentNumberBytes);
                    output.flush();
                    System.out.println("Student number sent to server: 554514");
                    studentNumberSent = true; // Mark the student number as sent
                }

                // Send the octal bytes when requested
                if (studentNumberSent && !octalBytesSent && message.contains("Here are three bytes in octal representation")) {
                    byte[] octalBytes = new byte[]{
                            (byte) 0103, // Octal 103
                            (byte) 0247, // Octal 247
                            (byte) 0231  // Octal 231
                    };
                    output.write(octalBytes);
                    output.flush();
                    System.out.println("Octal bytes sent to server: 103, 247, 231");
                    octalBytesSent = true; // Mark the octal bytes as sent
                }

                // Decode Base64 and send the raw output
                if (!base64Decoded && message.contains("The following data is encoded using base64 encoding")) {
                    // Extract the Base64 string dynamically
                    String base64EncodedData = message.substring(message.indexOf(":") + 1).trim();

                    // Decode the Base64 string
                    byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedData);

                    // Send the decoded raw data to the server
                    output.write(decodedBytes);
                    output.flush();

                    System.out.println("Decoded Base64 data sent to server.");
                    base64Decoded = true; // Mark the data as decoded and sent
                }

                if (!aesDecrypted && message.contains("AES-128 in ECB mode")) {
                    // Extract the Base64-encoded key and ciphertext
                    String base64Key = "cvVtQ55FZZ2c/wEFFe7KSg==";
                    String base64Ciphertext = "aV1QyC2KP8Ir1hxDUJEfdpSwR3LLsQxfD86zIKlecDidaPz0R/gBNiSSTkYufiM4cr/RGxC0/uJBDS1+oo/36hLjl/HswMbabZKD6CK02Ag=";

                    // Decode the Base64 key and ciphertext
                    byte[] secretKey = Base64.getDecoder().decode(base64Key);
                    byte[] ciphertext = Base64.getDecoder().decode(base64Ciphertext);

                    // Decrypt the ciphertext
                    byte[] decryptedMessage = decryptAES(secretKey, ciphertext);

                    // Send the decrypted message back to the server
                    output.write(decryptedMessage);
                    output.flush();

                    System.out.println("Decrypted message sent to server.");
                    aesDecrypted = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] decryptAES(byte[] secretKey, byte[] ciphertext) throws Exception {
        // Create AES key spec
        SecretKeySpec keySpec = new SecretKeySpec(secretKey, "AES");

        // Initialize cipher for AES in ECB mode
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        // Decrypt the ciphertext
        return cipher.doFinal(ciphertext);
    }
}
