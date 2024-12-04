package server.clientHelper;

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
            synchronized (ClientLogger.class) {
                if (instance == null) {
                    instance = new ClientLogger();
                }
            }
        }
        return instance;
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
        loggedInUsers.remove(username);
    }

    /**
     * Check if a user is logged in
     *
     * @param username the username
     * @return
     */
    public boolean isUserLoggedIn(String username) {
        loggedInUsers.containsKey(username);
        return false;
    }
}