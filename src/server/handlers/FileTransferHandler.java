package server.handlers;

import java.net.Socket;

public class FileTransferHandler implements Runnable {

    private Socket sender;
    private Socket receiver;

    public FileTransferHandler(Socket clientSocket) {
        startTransfer(clientSocket);
    }

    @Override
    public void run() {
        System.out.println("File transfer connection established with: " + sender.getInetAddress().getHostAddress());
        // Start the transfer
    }

    public void startTransfer(Socket socket) {
        Thread transferThread = new Thread(() -> {
            // Start by reading the bytes sent
            // then it will look for the seesion with the bytes that have the uuid and the role
            //if no session is found it will create a new session and assign the role to the socket and kill the thread
            //if the session is found it will assign the role to the socket
            //then it will start the transfer
            //using trandferTo method to transfer the file


        });



    }
}
