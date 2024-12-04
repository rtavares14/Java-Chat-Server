package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import shared.enumerations.ServerCommands;
import shared.messages.*;
import shared.utils.JsonUtils;
import shared.utils.MessageHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import static shared.enumerations.CmdColors.*;
import static shared.enumerations.ServerCommands.*;

public class ClientInstance implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private PrintWriter out;
    private BufferedReader in;
    private AtomicBoolean isRunning;
    private String username = "";
    private ClientHandler clientHandler = new ClientHandler(this);

    /**
     * Constructor for the ClientInstance class
     *
     * @param socket the client socket
     * @param server the server instance
     */
    public ClientInstance(Socket socket, Server server) {
        this.clientSocket = socket;
        this.server = server;
        this.isRunning = new AtomicBoolean(true);
    }

    /**
     * Run the client instance
     * This method is used to run the client instance
     * It sends a READY message to the client and then processes incoming messages
     * If the client disconnects, the method closes the input and output streams and the client socket
     * If an exception occurs, the method prints the stack trace
     */
    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Ready ready = new Ready(server.getVersion());
            String json = JsonUtils.toJson(ready);
            sendCommand(READY, json);

            MessageHelper.printColoredMessage(PURPLE, "S --> (): " + JsonUtils.toJson(ready));

            server.getClientUserCounts();

            String inputLine;
            while (isRunning.get() && (inputLine = in.readLine()) != null) {
                MessageHelper.printColoredMessage(PURPLE, "C (" + username + ") --> S: " + inputLine);
                processMessage(inputLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Process a message
     * This method is used to process a message
     * It splits the message into parts and then processes the command
     * If an exception occurs, the method prints an error message
     *
     * @param message the message to be processed
     */
    private void processMessage(String message) {
        try {
            String[] parts = message.split(" ", 2);
            ServerCommands command = ServerCommands.valueOf(parts[0]);
            String jsonPayload = parts.length > 1 ? parts[1] : "";

            switch (command) {
                case ENTER -> handleLogin(jsonPayload);
                case BROADCAST_REQ -> handleBroadcastReq(jsonPayload);
                case BYE -> handleLogout();
                default -> MessageHelper.printColoredMessage(RED, "Unknown command: " + command);
            }
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
        }
    }

    private void handleLogout() throws JsonProcessingException {
        ByeResp byeResp = new ByeResp("OK");
        sendCommand(BYE_RESP, JsonUtils.toJson(byeResp));
        MessageHelper.printColoredMessage(PURPLE, "S --> (" + username + "): " + JsonUtils.toJson(byeResp));

        Left left = new Left(username);
        server.broadcastMessage(left, username, LEFT);

        // Stop the thread and close the client instance
        isRunning.set(false);
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle a login message
     * This method is used to handle a login message
     * It deserializes the message and then processes the login
     * If an exception occurs, the method prints an error message
     *
     * @param jsonPayload the JSON payload of the message
     */
    private void handleLogin(String jsonPayload) throws JsonProcessingException {
        try {
            Enter enter = JsonUtils.fromJson(jsonPayload, Enter.class);
            EnterResp response = clientHandler.handleLogin(enter);
            sendCommand(ENTER_RESP, JsonUtils.toJson(response));
            MessageHelper.printColoredMessage(PURPLE, "S --> (" + username + "): " + JsonUtils.toJson(response));
            server.getClientUserCounts();
        } catch (Exception e) {
            EnterResp response = new EnterResp("ERROR", 5001);
            sendCommand(ENTER_RESP, JsonUtils.toJson(response));
            MessageHelper.printColoredMessage(RED, "S --> (): " + JsonUtils.toJson(response));}
    }

    /**
     * Handle a broadcast request
     * This method is used to handle a broadcast request
     * It deserializes the message and then processes the broadcast
     * If an exception occurs, the method prints an error message
     *
     * @param jsonPayload the JSON payload of the message
     */
    private void handleBroadcastReq(String jsonPayload) throws JsonProcessingException {
            BroadcastReq broadcastReq = JsonUtils.fromJson(jsonPayload, BroadcastReq.class);
            if (username == "" || username.isEmpty()) {
                BroadcastResp response = new BroadcastResp("ERROR", 6000);
                sendCommand(BROADCAST_RESP, JsonUtils.toJson(response));
                MessageHelper.printColoredMessage(RED, "S --> (): " + JsonUtils.toJson(response));
                return;
            }

            // Broadcast the message to all other clients
            Broadcast broadcast = new Broadcast(username, broadcastReq.getMessage());
            server.broadcastMessage(broadcast, username, BROADCAST);


        // Send confirmation to the sender
            BroadcastResp response = new BroadcastResp("OK", 0);
            sendCommand(BROADCAST_RESP, JsonUtils.toJson(response));
    }

    /**
     * Get the username
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the username
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Send a command to the client
     * This method is used to send a command to the client
     * It sends the command and the JSON payload to the client
     * If the command or payload is invalid, the method prints an error message
     *
     * @param command    the command to be sent
     * @param jsonPayload the JSON payload to be sent
     */
    void sendCommand(ServerCommands command, String jsonPayload) {
        if (command != null && jsonPayload != null && !jsonPayload.isEmpty()) {
            out.println(command + " " + jsonPayload);
        } else {
            System.err.println("Invalid command or payload. Cannot send to server.");
        }
    }
}