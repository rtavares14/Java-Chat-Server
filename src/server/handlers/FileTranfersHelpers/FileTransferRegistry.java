package server.handlers.FileTranfersHelpers;

import server.handlers.FileTransferHandler;
import shared.utils.messages.MessageHelper;

import java.util.concurrent.ConcurrentHashMap;

import static shared.enumerations.CmdColors.WHITE;

public class FileTransferRegistry {
    private static FileTransferRegistry instance;
    private static int totalTransfers;
    private final ConcurrentHashMap<String, FileTransferTimer> activeSessions; //timer with info
    private final ConcurrentHashMap<String, FileTransferHandler> activeHandlers; //session for transfer

    private FileTransferRegistry() {
        activeSessions = new ConcurrentHashMap<>();
        activeHandlers = new ConcurrentHashMap<>();
    }

    public static FileTransferRegistry getInstance() {
        if (instance == null) {
            instance = new FileTransferRegistry();
        }
        return instance;
    }

    /**
     * Add to the total transfers
     */
    public static void addToTT() {
        totalTransfers++;
    }

    /**
     * Subtract from the total transfers
     */
    public static void subFromTT() {
        totalTransfers--;
    }

    /**
     * Add a session to the registry
     *
     * @param uuid    the UUID
     * @param handler the handler
     */
    public void addSession(String uuid, FileTransferTimer handler) {
        activeSessions.put(uuid, handler);
    }

    /**
     * Add a handler to the registry
     *
     * @param uuid    the UUID
     * @param handler the handler
     */
    public void addHandler(String uuid, FileTransferHandler handler) {
        activeHandlers.put(uuid, handler);
    }

    /**
     * Get the handler from the registry
     *
     * @param uuid the UUID
     * @return the handler
     */
    public FileTransferHandler getHandler(String uuid) {
        return activeHandlers.get(uuid);
    }

    /**
     * Get the session from the registry
     *
     * @param uuid the UUID
     * @return the session
     */
    public FileTransferTimer getSession(String uuid) {
        return activeSessions.get(uuid);
    }

    /**
     * Remove a session from the registry
     *
     * @param uuid the UUID
     */
    public void removeSession(String uuid) {
        activeSessions.remove(uuid);
    }

    /**
     * Remove a handler from the registry
     *
     * @param uuid the UUID
     */
    public void removeHandler(String uuid) {
        activeHandlers.remove(uuid);
    }

    /**
     * Print the sessions
     */
    public void printSessions() {
        MessageHelper.printColoredMessage(WHITE, "Total successful transfers: " + totalTransfers);
    }
}
