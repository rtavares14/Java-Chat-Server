package server.handlers.FileTranfersHelpers;

import server.handlers.FileTransferHandler;
import shared.utils.messages.MessageHelper;

import java.util.concurrent.ConcurrentHashMap;

import static shared.enumerations.CmdColors.*;

public class FileTransferRegistry {
    private static FileTransferRegistry instance;
    private final ConcurrentHashMap<String, FileTransferTimer> activeSessions; //timer with info
    private final ConcurrentHashMap<String, FileTransferHandler> activeHandlers; //session for transfer
    private static int totalTransfers;

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

    public FileTransferHandler getHandler(String uuid) {
        return activeHandlers.get(uuid);
    }

    public FileTransferTimer getSession(String uuid) {
        return activeSessions.get(uuid);
    }

    public void removeSession(String uuid) {
        activeSessions.remove(uuid);
    }

    public void removeHandler(String uuid) {
        activeHandlers.remove(uuid);
    }

    public void printSessions() {
        MessageHelper.printColoredMessage(WHITE ,"Total successful transfers: " + totalTransfers);
    }
}
