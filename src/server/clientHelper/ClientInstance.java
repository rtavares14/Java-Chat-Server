package server.clientHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.Server;
import shared.enumerations.ServerCommands;
import shared.messages.*;
import shared.messages.List;
import shared.utils.JsonUtils;
import shared.utils.MessageHelper;

import java.io.BufferedReader;
import java.io.IOException;
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
        } catch (IOException e) {
            System.err.println("Client disconnected unexpectedly: " + e.getMessage());
            cleanup();
        } finally {
            cleanup();
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
    private void processMessage(String message) throws JsonProcessingException {
        try {
            String[] parts = message.split(" ", 2);
            ServerCommands command = ServerCommands.valueOf(parts[0]);
            String jsonPayload = parts.length > 1 ? parts[1] : "";

            switch (command) {
                case ENTER -> handleLogin(jsonPayload);
                case LIST_REQ -> handleListReq();
                case BROADCAST_REQ -> handleBroadcastReq(jsonPayload);
                case SENDTO_REQ -> handlePrivateMessage(jsonPayload);
                case BYE -> handleLogout();
                case PING -> sendCommand(ServerCommands.PONG, "");
            }
        } catch (IllegalArgumentException e) {
            invalidCommand();
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
        }
    }

    private void invalidCommand() {
        sendCommand(UNKNOWN_COMMAND, UNKNOWN_COMMAND.toString());
        MessageHelper.printColoredMessage(RED, "S --> (): " + UNKNOWN_COMMAND);
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

            if (response.getStatus().equals("OK")) {
                MessageHelper.printColoredMessage(GREEN, "S --> (" + username + "): " + JsonUtils.toJson(response));
                Joined joined = new Joined(username);
                server.broadcastMessage(joined, username, JOINED);
                server.getClientUserCounts();
            } else {
                MessageHelper.printColoredMessage(RED, "S --> (): " + JsonUtils.toJson(response));
            }


        } catch (Exception e) {
            EnterResp response = new EnterResp("ERROR", 5001);
            sendCommand(ENTER_RESP, JsonUtils.toJson(response));
            MessageHelper.printColoredMessage(RED, "S --> (): " + JsonUtils.toJson(response));
        }
    }

    /**
     * Handle a list request
     * This method is used to handle a list request
     * It deserializes the message and then processes the list request
     * If an exception occurs, the method prints an error message
     */
    private void handleListReq() throws JsonProcessingException {
        if (username == "" || username.isEmpty()) {
            ListResp response = new ListResp("ERROR", 6000);
            sendCommand(LIST_RESP, JsonUtils.toJson(response));
            MessageHelper.printColoredMessage(RED, "S --> (): " + JsonUtils.toJson(response));
            return;
        }

        ListResp response = new ListResp("OK", null);
        sendCommand(LIST_RESP, JsonUtils.toJson(response));

        List list = new List(ClientLogger.getInstance().getClients());
        sendCommand(LIST, JsonUtils.toJson(list));
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
        if (username == null || username.isEmpty()) {
            BroadcastResp response = new BroadcastResp("ERROR", 6000);
            sendCommand(BROADCAST_RESP, JsonUtils.toJson(response));
            MessageHelper.printColoredMessage(RED, "S --> (): " + JsonUtils.toJson(response));
            return;
        }

        // Broadcast the message to all other clients
        Broadcast broadcast = new Broadcast(username, broadcastReq.getMessage());
        server.broadcastMessage(broadcast, username, BROADCAST);


        // Send confirmation to the sender
        BroadcastResp response = new BroadcastResp("OK", null);
        sendCommand(BROADCAST_RESP, JsonUtils.toJson(response));
    }

    /**
     * Handle a logout message
     * This method is used to handle a logout message
     * It sends a BYE_RESP message to the client and then broadcasts a LEFT message to all other clients
     * If an exception occurs, the method prints an error message
     */

    private void handlePrivateMessage(String jsonPayload) throws JsonProcessingException {
        SendToReq sendToReq = JsonUtils.fromJson(jsonPayload, SendToReq.class);
        String receiver = sendToReq.getUsername();
        String content = sendToReq.getMessage();

        if (username == "" || username.isEmpty()) {
            SendToResp response = new SendToResp("ERROR", 6000);
            sendCommand(SENDTO_RESP, JsonUtils.toJson(response));
            MessageHelper.printColoredMessage(RED, "S --> (): " + SENDTO_RESP + " " + JsonUtils.toJson(response));
            return;
        }

        // Send the message to the receiver
        SendTo sendTo = new SendTo(username, content);
        String json = JsonUtils.toJson(sendTo);

        ClientInstance receiverInstance = ClientLogger.getInstance().getClient(receiver);
        if (receiverInstance == null) {
            SendToResp response = new SendToResp("ERROR", 6006);
            sendCommand(SENDTO_RESP, JsonUtils.toJson(response));
            MessageHelper.printColoredMessage(RED, "S --> (" + username + "): " + SENDTO_RESP + " " + JsonUtils.toJson(response));
        } else {
            receiverInstance.sendCommand(SENDTO, json);
            MessageHelper.printColoredMessage(WHITE, "C (" + username + ") --> C (" + receiver + "): " + SENDTO + " : " + jsonPayload);


            // Send confirmation to the sender
            SendToResp response = new SendToResp("OK", null);
            sendCommand(SENDTO_RESP, JsonUtils.toJson(response));
        }
    }

    private void handleLogout() throws JsonProcessingException {
        ByeResp byeResp = new ByeResp("OK");
        sendCommand(BYE_RESP, JsonUtils.toJson(byeResp));
        MessageHelper.printColoredMessage(PURPLE, "S --> (" + username + "): " + JsonUtils.toJson(byeResp));

        Left left = new Left(username);
        server.broadcastMessage(left, username, LEFT);

        // Remove the client from the allClients list
        ClientLogger.getInstance().getAllClients().remove(this);

        // Stop the thread and close the client instance
        isRunning.set(false);
        server.getClientUserCounts();
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * @param command     the command to be sent
     * @param jsonPayload the JSON payload to be sent
     */
    public void sendCommand(ServerCommands command, String jsonPayload) {
        if (command != null && jsonPayload != null && !jsonPayload.isEmpty()) {
            out.println(command + " " + jsonPayload);
        } else {
            System.err.println("Invalid command or payload. Cannot send to server.");
        }
    }

    private void cleanup() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        } finally {
            ClientLogger.getInstance().removeUser(username);
        }
    }
}