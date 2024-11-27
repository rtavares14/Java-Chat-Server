package server;

import client.Client;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private final int PORT = 1337;
    private ServerSocket serverSocket;

    //private ConcurrentHashMap<,> activeUsers = new ConcurrentHashMap<>();

    private final String VERSION = "RCT 1.0";
    private boolean ShouldPing = false;

    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                //ClientHandler clientHandler = new ClientHandler(clientSocket);
               // new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}