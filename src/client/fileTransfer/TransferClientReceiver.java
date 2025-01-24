package client.fileTransfer;

import client.inputs.UserInput;
import shared.messages.file_transfer.status.FileTransferCheckReq;
import shared.utils.JsonUtils;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static shared.enumerations.ServerCommands.FILET_CHECK_REQ;

public class TransferClientReceiver implements Runnable {
    private final String checksumOld;
    private final String uuid;
    private final File file;
    private Socket socket;

    public TransferClientReceiver(String checksumOld, String uuid, String fileName) {
        this.checksumOld = checksumOld;
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
            // Generate checksum

            fileOutputStream.close();

            String checksumNew = createChecksum(file);

            if (checksumOld.equals(checksumNew)) {
                FileTransferCheckReq fileTransferCheckReq = new FileTransferCheckReq("GOOD", uuid);
                String json = JsonUtils.toJson(fileTransferCheckReq);
                UserInput.sendCommand(FILET_CHECK_REQ, json);

            } else {
                FileTransferCheckReq fileTransferCheckReq = new FileTransferCheckReq("BAD", uuid);
                String json = JsonUtils.toJson(fileTransferCheckReq);
                UserInput.sendCommand(FILET_CHECK_REQ, json);
                //delete file
                file.delete();
            }

            inputStream.close();
            outputStream.close();
            socket.close();

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