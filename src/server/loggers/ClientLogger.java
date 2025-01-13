package server.loggers;

import server.clientInstance.ClientInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClientLogger {

    private static ClientLogger instance;
    private final ConcurrentHashMap<String, ClientInstance> loggedInUsers = new ConcurrentHashMap<>();
    private final List<ClientInstance> allClients = new ArrayList<>();

    /**
     * Constructor for the ClientLogger class
     */
    private ClientLogger() {
    }

    /**
     * Get the instance of the ClientLogger
     *
     * @return ClientLogger
     */
    public static ClientLogger getInstance() {
        if (instance == null) {
            instance = new ClientLogger();
        }
        return instance;
    }

    /**
     * Check if the username is valid
     *
     * @param username the username to be checked
     * @return true if the username is valid, false otherwise
     */
    public static boolean isUsernameValid(String username) {
        return !username.isEmpty() && username.matches("^[A-Za-z0-9_]{3,14}$");
    }

    /**
     * Check if the username is available
     *
     * @param username the username to be checked
     * @return true if the username is available, false otherwise
     */
    public static boolean isUsernameAvailable(String username) {
        return !ClientLogger.getInstance().isUserLoggedIn(username);
    }

    /**
     * Check if the username is valid and available
     *
     * @param username the username to be checked
     * @return 5000 if the username is already taken,
     * 5001 if the username is invalid,
     * 5002 if the username is valid but already logged in
     */
    public static int checkUsername(String username) {
        if (!isUsernameValid(username)) {
            return 5001;
        } else if (!isUsernameAvailable(username)) {
            return 5000;
        } else {
            return 5002;
        }
    }

    /**
     * Get a client instance
     *
     * @param receiver the receiver
     * @return the client instance
     */
    public ClientInstance getClient(String receiver) {
        return loggedInUsers.get(receiver);
    }

    /**
     * Get logged-in users
     *
     * @return ConcurrentHashMap of logged-in users
     */
    public ConcurrentHashMap<String, ClientInstance> getLoggedInUsers() {
        return loggedInUsers;
    }

    /**
     * Get all clients
     *
     * @return List of all clients
     */
    public List<ClientInstance> getAllClients() {
        return allClients;
    }

    /**
     * Get all clients
     *
     * @return List of all clients
     */
    public ArrayList<String> getClients() {
        ArrayList<String> clients = new ArrayList<>();
        for (ClientInstance client : allClients) {
            clients.add(client.getUsername());
        }
        return clients;
    }

    /**
     * Add a client to the all clients list
     *
     * @param client the client instance
     */
    public void addClient(ClientInstance client) {
        allClients.add(client);
    }

    /**
     * Add a user to the logged-in users list
     *
     * @param username the username
     * @param instance the client instance
     */
    public void logInUser(String username, ClientInstance instance) {
        loggedInUsers.put(username, instance);
    }

    /**
     * Remove a user from the logged-in users list
     *
     * @param username the username
     */
    public void removeUser(String username) {
        allClients.remove(getClient(username));
        loggedInUsers.remove(username);
    }

    /**
     * Close all clients
     */
    public void closeAllClients() {
        for (ClientInstance client : allClients) {
            removeUser(client.getUsername());
        }
    }

    /**
     * Check if a user is logged in
     *
     * @param username the username
     * @return true if the user is logged in, false otherwise
     */
    public boolean isUserLoggedIn(String username) {
        return loggedInUsers.containsKey(username);
    }
}