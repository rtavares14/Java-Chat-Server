package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import shared.enumerations.ServerCommands;
import shared.messages.*;
import shared.utils.*;


import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static shared.enumerations.CmdColors.*;
import static shared.enumerations.ServerCommands.*;

public class Server {

    private final int PORT = 1337;
    private ServerSocket serverSocket;

    private static final ConcurrentHashMap<String, ClientInstance> loggedInUsers = new ConcurrentHashMap<>();
    private static final List<ClientInstance> allClients = new ArrayList<>();

    private final String VERSION = "RCT 1.1";

    /**
     * Start the server
     * This method is used to start the server
     */
    public void startingServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            MessageHelper.printColoredMessage(PURPLE,"Starting server version (" + VERSION + ") on port: " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                MessageHelper.printColoredMessage(PURPLE,"New client connected: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                ClientInstance clientInstance = new ClientInstance(clientSocket, this);
                allClients.add(clientInstance);
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
     * Get all logged in users
     * This method is used to get all logged in users
     *
     * @return ConcurrentHashMap<String, ClientInstance>
     */
    public static ConcurrentHashMap<String, ClientInstance> getLoggedInUsers() {
        return loggedInUsers;
    }

    /**
     * Log in user
     * This method is used to log in a user
     *
     * @param username the username of the user
     * @param handler the client instance
     */
    public static void logInUser(String username, ClientInstance handler) {
        loggedInUsers.put(username, handler);
    }

    /**
     * Remove user
     * This method is used to remove a user
     *
     * @param username the username of the user
     */
    public static void removeUser(String username) {
        loggedInUsers.remove(username);
    }

    /**
     * Check if user is logged in
     * This method is used to check if a user is logged in
     *
     * @param username the username of the user
     * @return boolean
     */
    public static boolean isLoggedIn(String username) {
        return loggedInUsers.containsKey(username);
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
        MessageHelper.printColoredMessage(GREEN, loggedInUsers.size() + " client(s) / " +  allClients.size() + " user(s)");
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
        for (ClientInstance client : loggedInUsers.values()) {
            if (!client.getUsername().equals(senderUsername)) {
                client.sendCommand(command, jsonMessage);
            }
        }
        if (command == LEFT) {
            removeUser(senderUsername);
            MessageHelper.printColoredMessage(YELLOW, "S --> (ALL): " + jsonMessage);
        } else {
            MessageHelper.printColoredMessage(ORANGE, "S --> (ALL): " + jsonMessage);
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