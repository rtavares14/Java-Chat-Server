package server.clientInstance;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.Server;
import server.handlers.HeartbeatHandler;
import server.loggers.ClientLogger;
import server.loggers.ServerLogger;
import shared.enumerations.ServerCommands;
import shared.messages.general.Ready;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHandler;
import shared.utils.messages.MessageHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import static shared.enumerations.CmdColors.PURPLE;
import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.*;

public class ClientInstance implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private final AtomicBoolean isRunning;
    private final boolean normalDisconnection = true;
    private MessageHandler messageHandler;
    private PrintWriter out;
    private BufferedReader in;
    private String username = "";
    private boolean expectingPong = false;
    private boolean pingPongEnabled = true;

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
     * Check if the client is expecting a pong
     *
     * @return the expecting pong status
     */
    public boolean isExpectingPong() {
        return expectingPong;
    }

    /**
     * Set the expecting pong status
     *
     * @param expectingPong the expecting pong status
     */
    public void setExpectingPong(boolean expectingPong) {
        this.expectingPong = expectingPong;
    }

    /**
     * Get the output stream
     *
     * @return the output stream
     */
    public PrintWriter getOut() {
        return out;
    }

    /**
     * Set the ping pong enabled status
     *
     * @return the ping pong enabled status
     */
    public boolean isPingPongEnabled() {
        return pingPongEnabled;
    }

    /**
     * Return the client socket
     *
     * @return the client socket
     */
    public Socket getSocket() {
        return clientSocket;
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

            this.messageHandler = new MessageHandler(out, this);

            Ready ready = new Ready(server.getVersion());
            String json = JsonUtils.toJson(ready);
            sendCommand(READY, json);

            MessageHelper.printServerMessage(PURPLE, this, READY, JsonUtils.toJson(ready));

            ServerLogger.getInstance().getClientUserCounts();

            String inputLine;
            while (isRunning.get() && (inputLine = in.readLine()) != null) {
                MessageHelper.printColoredMessage(PURPLE, "C (" + username + ") --> S: " + inputLine);
                processUserMessage(inputLine);
            }
        } catch (IOException e) {
            if (!normalDisconnection) {
                System.err.println("Client disconnected unexpectedly: " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }

    /**
     * Process the message
     * This method is used to process the message received from the client
     * It splits the message into command and payload and handles the message
     * If an error occurs, the method prints and sends an unknown command
     *
     * @param message the message to be processed
     */
    private void processUserMessage(String message) {
        try {
            // Split the message into command and payload
            String[] parts = message.split(" ", 2);
            String commandString = parts[0];
            String payload = parts.length > 1 ? parts[1] : "";

            // Check if the command exists in ServerCommands enum
            ServerCommands command;
            try {
                command = ServerCommands.valueOf(commandString);
            } catch (IllegalArgumentException e) {
                // Command is not recognized, send UNKNOWN_COMMAND
                out.println(UNKNOWN_COMMAND);
                MessageHelper.printColoredMessage(RED, "S --> (" + username + "): " + UNKNOWN_COMMAND);
                return;
            }

            // Try to handle the command
            messageHandler.handleClientMessage(command, payload);

        } catch (JsonProcessingException e) {
            out.println(PARSE_ERROR);
            MessageHelper.printServerMessage(RED, this, PARSE_ERROR, PARSE_ERROR.toString());
        }
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

    /**
     * Cleanup the client instance
     * This method is used to clean up the client instance
     * It closes the input and output streams and the client socket
     * If an exception occurs, the method prints an error message
     */
    public void cleanup() {
        isRunning.set(false);
        try {
            HeartbeatHandler.getInstance().stopHeartbeat(this);
            if (clientSocket != null) clientSocket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        } finally {
            ClientLogger.getInstance().removeUser(username);
            ClientLogger.getInstance().getAllClients().remove(this);
            if (!normalDisconnection) {
                MessageHelper.printColoredMessage(RED, "Client disconnected unexpectedly: " + username);
                ServerLogger.getInstance().getClientUserCounts();
            }
        }
    }
}