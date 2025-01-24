package server.handlers.FileTranfersHelpers;

import server.handlers.FileTransferHandler;
import shared.utils.messages.MessageHelper;

import java.net.ServerSocket;
import java.net.Socket;

import static shared.enumerations.CmdColors.RED;

public class FileTransferSv implements Runnable {
    private final int FILE_PORT;

    public FileTransferSv(int FILE_PORT) {
        this.FILE_PORT = FILE_PORT;
    }

    @Override
    public void run() {
        try (ServerSocket fileTransferSocket = new ServerSocket(FILE_PORT)) {
            FileTransferRegistry.getInstance().printSessions();
            while (fileTransferSocket.isBound()) {
                Socket fileTransferClient = fileTransferSocket.accept();
                new Thread(new FileTransferHandler(fileTransferClient)).start();
            }
        } catch (Exception e) {
            MessageHelper.printColoredMessage(RED, "Error starting file server on port: " + FILE_PORT);
        }
    }
}
