package client.fileTransfer;

import java.io.*;
import java.net.Socket;

public class TransferClientSender implements Runnable {
    private final String uuid;
    private final File file;
    private Socket socket;

    public TransferClientSender(String uuid, File file) {
        this.uuid = uuid;
        this.file = file;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            socket = new Socket("localhost", 1338);
            filetStart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the file to the server
     * Send file with method transferTo
     */
    public void filetStart() {
        try (InputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = socket.getOutputStream()) {
            outputStream.write((uuid + "S").getBytes());
            outputStream.flush();

            inputStream.transferTo(outputStream);

            inputStream.close();
            outputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
