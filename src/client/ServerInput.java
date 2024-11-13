package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.ServerMessage;
import utils.MessageHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerInput implements Runnable {

    private Socket socket;

    public ServerInput(Socket socket) {
        this.socket = socket;
    }

    /**
     * Receives messages from the server and handles them.
     */
    @Override
    public void run() {
        try {
            InputStream is = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String serverMessage;
            while ((serverMessage = reader.readLine()) != null) {
                ServerMessage message = parseServerMessage(serverMessage);
                handleServerMessage(message);
            }

        } catch (IOException e) {
            System.err.println("Error receiving message: " + e.getMessage());
        }
    }

    /**
     * Parses a message from the server into a ServerMessage object.
     * @param message the message from the server.
     * @return the ServerMessage object.
     */
    private ServerMessage parseServerMessage(String message) {
        String[] parts = message.split(" ", 2);
        String type = parts[0];
        String body = parts.length > 1 ? parts[1] : "";

        JsonNode data = null;
        try {
            data = new ObjectMapper().readTree(body);
        } catch (IOException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }

        return new ServerMessage(type, data);
    }

    /**
     * Handles a message from the server.
     * @param message the message from the server.
     */
    private void handleServerMessage(ServerMessage message) {
        String response = MessageHandler.determineMessage(message);
        System.out.println(response);
    }
}