package client.inputs;

import shared.enumerations.ServerCommands;
import shared.utils.messages.MessageHandler;

import java.io.*;
import java.net.Socket;

public class ServerInput implements Runnable {

    private final MessageHandler messageHandler;
    /**
     * The socket to connect to the server
     * The writer to send messages to the server
     * The message handler to process server messages
     */
    private Socket socket;
    private PrintWriter writer;

    /**
     * Constructs a new ServerInput object.
     *
     * @param socket the socket.
     * @throws IOException if an I/O error occurs.
     */
    public ServerInput(Socket socket) throws IOException {
        this.socket = socket;
        OutputStream out = socket.getOutputStream();
        this.writer = new PrintWriter(out, true);
        this.messageHandler = new MessageHandler(writer, null);
    }

    /**
     * Receives messages from the server and handles them.
     * Read messages from the server
     * Process the server message
     * If an error occurs, print the error message
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

    /**
     * Processes the server message.
     * Split the message into command and payload
     * Handle the server message
     * If an error occurs, print the error message
     *
     * @param message the message from the server.
     */
    private void processServerMessage(String message) {
        // Split the message into command and payload
        // Handle the server message
        // If an error occurs, print the error message
        try {
            String[] parts = message.split(" ", 2);
            ServerCommands command = ServerCommands.valueOf(parts[0]);
            String jsonPayload = parts.length > 1 ? parts[1] : "";

            messageHandler.handleServerMessage(command, jsonPayload);
        } catch (Exception e) {
            System.err.println("Failed to process server message: " + e.getMessage());
        }
    }
}