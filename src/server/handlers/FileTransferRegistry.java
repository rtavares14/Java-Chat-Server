package server.handlers;

import shared.utils.messages.MessageHelper;

import java.util.concurrent.ConcurrentHashMap;

import static shared.enumerations.CmdColors.*;

public class FileTransferRegistry {
    private static FileTransferRegistry instance;
    private final ConcurrentHashMap<String, FileTransferHandler> activeTransfers;
    private static int totalTransfers;

    private FileTransferRegistry() {
        activeTransfers = new ConcurrentHashMap<>();
    }

    public static synchronized FileTransferRegistry getInstance() {
        if (instance == null) {
            instance = new FileTransferRegistry();
        }
        return instance;
    }

    public static int getTT() {
        return totalTransfers;
    }

    public static void addToTT() {
        totalTransfers++;
    }

    public static void subFromTT() {
        totalTransfers--;
    }

    public void addSession(String uuid, FileTransferHandler handler) {
        activeTransfers.put(uuid, handler);
    }

    public FileTransferHandler getSession(String uuid) {
        return activeTransfers.get(uuid);
    }

    public void removeSession(String uuid) {
        activeTransfers.remove(uuid);
    }

    public void printSessions() {
        MessageHelper.printColoredMessage(WHITE ,"Active file transfers: " + activeTransfers.size() + ". Total transfers: " + totalTransfers);
    }
}
