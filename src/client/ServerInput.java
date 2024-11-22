package client;

import shared.enumerations.ServerCommands;
import shared.utils.MessageHandler;

import java.io.*;
import java.net.Socket;

public class ServerInput implements Runnable {

    private Socket socket;
    private PrintWriter writer;

    public ServerInput(Socket socket) throws IOException {
        this.socket = socket;
        OutputStream out = socket.getOutputStream();
        this.writer = new PrintWriter(out, true);
    }

    /**
     * Receives messages from the server and handles them.
     */
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String serverMessage;
            while ((serverMessage = reader.readLine()) != null) {
                processServerMessage(serverMessage);
            }
        } catch (IOException e) {
            System.err.println("Error receiving message: " + e.getMessage());
        }
    }

    private void processServerMessage(String message) {
        try {
            String[] parts = message.split(" ", 2);
            ServerCommands command = ServerCommands.valueOf(parts[0]);
            String jsonPayload = parts.length > 1 ? parts[1] : "";

            MessageHandler.handleMessage(command, jsonPayload, writer);
        } catch (Exception e) {
            System.err.println("Failed to process server message: " + e.getMessage());
        }
    }
}