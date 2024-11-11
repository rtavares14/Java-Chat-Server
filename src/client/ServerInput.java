package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerInput implements Runnable {

    private Socket socket;

    public ServerInput(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            //input stream from the socket for receiving messages
            InputStream is = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            //print messages from the server
            String serverMessage;
            while ((serverMessage = reader.readLine()) != null) {
                System.out.println("Server: " + serverMessage);
            }

        } catch (IOException e) {
            System.err.println("Error receiving message: " + e.getMessage());
        }
    }
}
