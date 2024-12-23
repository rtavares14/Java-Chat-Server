package server.clientHelper;

import server.Server;
import server.loggers.ClientLogger;
import server.loggers.ServerLogger;
import shared.enumerations.ServerCommands;
import shared.messages.Ready;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHandler;
import shared.utils.messages.MessageHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static shared.enumerations.CmdColors.PURPLE;
import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.*;

public class ClientInstance implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private final AtomicBoolean isRunning;
    private final MessageHandler messageHandler;
    private final boolean normalDisconnection = true;
    private PrintWriter out;
    private BufferedReader in;
    private String username = "";
    private Timer heartbeatTimer;
    private boolean expectingPong = false;
    private boolean pingPongEnabled = false;

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
        this.messageHandler = new MessageHandler(out, this);
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

    public synchronized void setExpectingPong(boolean expectingPong) {
        this.expectingPong = expectingPong;
    }

    public synchronized boolean isExpectingPong() {
        return expectingPong;
    }

    public synchronized void setHeartbeatTimer(Timer heartbeatTimer) {
        this.heartbeatTimer = heartbeatTimer;
    }

    public PrintWriter getOut() {
        return out;
    }

    public boolean isPingPongEnabled() {
        return pingPongEnabled;
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
            String[] parts = message.split(" ", 2);
            ServerCommands command = ServerCommands.valueOf(parts[0]);

            if (command == PONG) {
                synchronized (this) {
                    if (expectingPong) {
                        expectingPong = false;
                    } else {
                        out.println(PONG_ERROR);
                        MessageHelper.printColoredMessage(RED, "S --> (" + getUsername() + "): " + PONG_ERROR);
                    }
                }
                return;
            }

            // Handle other commands
            messageHandler.handleClientMessage(command, parts.length > 1 ? parts[1] : "");
        } catch (Exception e) {
            out.println(UNKNOWN_COMMAND);
            MessageHelper.printColoredMessage(RED, "S --> (): " + UNKNOWN_COMMAND);
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
     * This method is used to cleanup the client instance
     * It closes the input and output streams and the client socket
     * If an exception occurs, the method prints an error message
     */
    public void cleanup() {
        try {
            System.out.println("Cleaning up resources for " + username);
            if (heartbeatTimer != null) heartbeatTimer.cancel();
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
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