package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.clientHelper.ClientInstance;
import server.logger.ClientLogger;
import shared.enumerations.ServerCommands;
import shared.utils.*;
import shared.utils.messages.MessageHelper;

import java.net.ServerSocket;
import java.net.Socket;

import static shared.enumerations.CmdColors.*;
import static shared.enumerations.ServerCommands.*;

public class Server {

    private final int SERVER_PORT = 1337;
    private final int FILE_PORT = 1338;
    private ServerSocket serverSocket;
    private ServerSocket fileServerSocket;

    private final String VERSION = "RCT Chat Server V1.14";

    /**
     * Start the server
     * This method is used to start the server
     */
    public void startingServer() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            MessageHelper.printColoredMessage(PURPLE,"Starting server version (" + VERSION + ") on port: " + SERVER_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                MessageHelper.printColoredMessage(PURPLE,"New client connected: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                ClientInstance clientInstance = new ClientInstance(clientSocket, this);
                ClientLogger.getInstance().addClient(clientInstance);
                new Thread(clientInstance).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    /**
     * Stop the server
     * This method is used to stop the server
     */
    public void stopServer() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get version
     * This method is used to get the version of the server
     *
     * @return String
     */
    public String getVersion() {
        return VERSION;
    }

    /**
     * Get client user counts
     * This method is used to get the client user counts
     *
     * @return String
     */
    public void getClientUserCounts() {
        MessageHelper.printColoredMessage(GREEN, ClientLogger.getInstance().getLoggedInUsers().size() + " client(s) / " +  ClientLogger.getInstance().getAllClients().size() + " user(s)");
    }

    /**
     * Broadcast message
     * This method is used to broadcast a message to all users
     *
     * @param message the message to be broadcasted
     * @param senderUsername the username of the sender
     * @param command the command
     * @throws JsonProcessingException if an exception occurs
     */
    public void broadcastMessage(Object message, String senderUsername, ServerCommands command) throws JsonProcessingException {
        String jsonMessage = JsonUtils.toJson(message);
        for (ClientInstance client : ClientLogger.getInstance().getLoggedInUsers().values()) {
            if (!client.getUsername().equals(senderUsername)) {
                client.sendCommand(command, jsonMessage);
            }
        }
        if (command == LEFT) {
            MessageHelper.printColoredMessage(YELLOW, "S --> (ALL): " + command + " " + jsonMessage);
        } else if (command == JOINED){
            MessageHelper.printColoredMessage(BLUE, "S --> (ALL): " + command + " " + jsonMessage);
        }else {
            MessageHelper.printColoredMessage(ORANGE, "S --> (ALL): " + command + " " + jsonMessage);
        }
    }

    /**
     * Main method
     * This method is used to start the server
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.startingServer();
    }
}