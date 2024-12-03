package server;

import shared.utils.MessageHelper;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import static shared.enumerations.CmdColors.*;

public class Server {

    private final int PORT = 1337;
    private ServerSocket serverSocket;

    private static final ConcurrentHashMap<String, ClientInstance> loggedInUsers = new ConcurrentHashMap<>();

    private final String VERSION = "RCT 1.1";
    private boolean ShouldPing = false;

    public void startingServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            MessageHelper.printColoredMessage(PURPLE,"Starting server version (" + VERSION + ") on port: " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                MessageHelper.printColoredMessage(PURPLE,"New client connected: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                ClientInstance clientInstance = new ClientInstance(clientSocket, this);
                new Thread(clientInstance).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ConcurrentHashMap<String, ClientInstance> getLoggedInUsers() {
        return loggedInUsers;
    }

    public static void logInUser(String username, ClientInstance handler) {
        loggedInUsers.put(username, handler);
    }

    public static void removeUser(String username) {
        loggedInUsers.remove(username);
    }

    public static boolean isLoggedIn(String username) {
        return loggedInUsers.containsKey(username);
    }

    public String getVersion() {
        return VERSION;
    }

    public String getClientUserCounts() {
        int totalClients = loggedInUsers.size();
        long loggedInUsersCount = loggedInUsers.values().stream()
                .filter(handler -> handler.getUsername() != null && !handler.getUsername().isEmpty())
                .count();
        return totalClients + " client(s) / " +  loggedInUsersCount + " user(s)";
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startingServer();
    }
}