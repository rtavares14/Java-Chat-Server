package server.handlers;

import server.handlers.FileTranfersHelpers.FileTransferRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class FileTransferHandler implements Runnable {

    private Socket sender;
    private Socket receiver;
    private Socket clientSocket;

    public FileTransferHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        startTransfer(clientSocket);
    }

    public void startTransfer(Socket socket) {
        try {
            InputStream inputStream = socket.getInputStream();

            // Read UUID and Role
            byte[] UUID = new byte[8];
            int bytesRead = inputStream.read(UUID);

            System.out.println("Raw UUID bytes: " + new String(UUID, StandardCharsets.UTF_8));
            String uuidString = new String(UUID, StandardCharsets.UTF_8);
            String uuid = uuidString.substring(0, uuidString.length() - 1);
            char roleChar = uuidString.charAt(uuidString.length() - 1);

            System.out.println("UUID: " + uuid);
            System.out.println("Role: " + roleChar);

            // Check if session exists in the registry
            FileTransferHandler existingHandler = FileTransferRegistry.getInstance().getHandler(uuid);

            if (existingHandler == null) {
                // Create a new session if none exists
                System.out.println("Session not found. Creating a new one for UUID: " + uuid);
                FileTransferRegistry.getInstance().addHandler(uuid, this);

                if (roleChar == 'S') {
                    setSender(socket);
                    System.out.println("Sender role assigned to UUID: " + uuid);
                } else if (roleChar == 'R') {
                    setReceiver(socket);
                    System.out.println("Receiver role assigned to UUID: " + uuid);
                }

                // Inform that you are waiting for the other role
                System.out.println("Waiting for the other role to connect for UUID: " + uuid);

            } else {
                // Assign the role to the existing session
                System.out.println("Session found for UUID: " + uuid);

                if (roleChar == 'S') {
                    existingHandler.setSender(socket);
                    System.out.println("Sender role assigned to UUID: " + uuid);
                } else if (roleChar == 'R') {
                    existingHandler.setReceiver(socket);
                    System.out.println("Receiver role assigned to UUID: " + uuid);
                }

                existingHandler.transferFile();

            }
        } catch (SocketTimeoutException e) {
            System.err.println("Socket timed out while reading UUID and role!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Transfer logic
    private void transferFile() {
        try (InputStream inputStream = sender.getInputStream();
             OutputStream outputStream = receiver.getOutputStream()) {

            System.out.println("Transferring file...");
            inputStream.transferTo(outputStream);
            System.out.println("File transfer completed!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSender(Socket sender) {
        this.sender = sender;
    }

    public void setReceiver(Socket receiver) {
        this.receiver = receiver;
    }
}
