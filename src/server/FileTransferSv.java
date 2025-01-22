package server;

import server.handlers.FileTranfersHelpers.FileTransferRegistry;
import server.handlers.FileTransferHandler;
import shared.utils.messages.MessageHelper;

import java.net.ServerSocket;
import java.net.Socket;

import static shared.enumerations.CmdColors.*;

public class FileTransferSv implements Runnable{
    private final int FILE_PORT;

    public FileTransferSv(int FILE_PORT){
        this.FILE_PORT = FILE_PORT;
    }

    @Override
    public void run(){
        try(ServerSocket fileTransferSocket = new ServerSocket(FILE_PORT)){
            while(true){
                Socket fileTransferClient = fileTransferSocket.accept();
                MessageHelper.printColoredMessage(PURPLE, "File transfer connection established: " + fileTransferSocket.getInetAddress().getHostAddress());
                FileTransferRegistry.getInstance().printSessions();

                new Thread(new FileTransferHandler(fileTransferClient)).start();
            }
        }catch(Exception e){
            MessageHelper.printColoredMessage(RED, "Error starting file server on port: " + FILE_PORT);
        }
    }
}
