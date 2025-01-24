package client.fileTransfer;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TransferClientReceiver implements Runnable {
    private final String uuid;
    private final File file;
    private Socket socket;

    public TransferClientReceiver(String uuid, String fileName) {
        this.uuid = uuid;
        this.file = new File("src/transferredFiles/" + fileName);
        new File("src/transferredFiles/" + fileName);
    }

    @Override
    public void run() {
        try {
            socket = new Socket("localhost", 1338);
            filetStart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void filetStart() {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            OutputStream fileOutputStream = new FileOutputStream(file);
            // Send UUID and role to server
            outputStream.write((uuid + "R").getBytes());
            outputStream.flush();

            // Start receiving file
            inputStream.transferTo(fileOutputStream);
            System.out.println("File transfer complete!");
            // Generate checksum

            fileOutputStream.close();
            inputStream.close();
            outputStream.close();
            socket.close();

            String checksum = createChecksum(file);
            System.out.println("Checksum: " + checksum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a checksum for the file
     *
     * @param file the path to the file
     * @return the checksum
     * @throws IOException              if the file cannot be read
     * @throws NoSuchAlgorithmException if the algorithm is not found
     */
    private String createChecksum(File file) throws IOException, NoSuchAlgorithmException {
        byte[] data = Files.readAllBytes(file.toPath());
        byte[] hash = MessageDigest.getInstance("MD5").digest(data);
        return new BigInteger(1, hash).toString(16);
    }
}