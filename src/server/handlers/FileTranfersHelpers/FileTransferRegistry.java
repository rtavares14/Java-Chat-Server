package server.handlers.FileTranfersHelpers;

import server.handlers.FileTransferHandler;
import shared.utils.messages.MessageHelper;

import java.util.concurrent.ConcurrentHashMap;

import static shared.enumerations.CmdColors.*;

public class FileTransferRegistry {
    private static FileTransferRegistry instance;
    private final ConcurrentHashMap<String, FileTransferTimer> activeSessions;
    private final ConcurrentHashMap<String, FileTransferHandler> activeHandlers;
    private static int totalTransfers;

    private FileTransferRegistry() {
        activeSessions = new ConcurrentHashMap<>();
        activeHandlers = new ConcurrentHashMap<>();
    }

    public static synchronized FileTransferRegistry getInstance() {
        if (instance == null) {
            instance = new FileTransferRegistry();
        }
        return instance;
    }

    public static void addToTT() {
        totalTransfers++;
    }

    public static void subFromTT() {
        totalTransfers--;
    }

    public void addSession(String uuid, FileTransferTimer handler) {
        activeSessions.put(uuid, handler);
    }

    public void addHandler(String uuid, FileTransferHandler handler) {
        activeHandlers.put(uuid, handler);
    }

    public FileTransferTimer getSession(String uuid) {
        return activeSessions.get(uuid);
    }

    public void removeSession(String uuid) {
        activeSessions.remove(uuid);
    }

    public void printSessions() {
        MessageHelper.printColoredMessage(WHITE ,"Active file transfers: " + activeSessions.size() + ". Total transfers: " + totalTransfers);
    }
}
