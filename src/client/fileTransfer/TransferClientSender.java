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
            socket = new Socket("localhost", 1338);
            filetStart();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void filetStart() {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
             OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream())) {
            outputStream.write((uuid + "S").getBytes());
            outputStream.flush();

            inputStream.transferTo(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
