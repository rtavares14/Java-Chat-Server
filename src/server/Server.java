package server;

import shared.utils.MessageWriter;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import static shared.enumerations.CmdColors.*;

public class Server {

    private final int PORT = 1337;
    private ServerSocket serverSocket;

    //private ConcurrentHashMap<,> activeUsers = new ConcurrentHashMap<>();

    private final String VERSION = "RCT 1.0";
    private boolean ShouldPing = false;

    public void startingServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            MessageWriter.printColoredMessage(PURPLE,"Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();

                MessageWriter.printColoredMessage(GREEN,"New client connected: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
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

    public static void main(String[] args) {
        Server server = new Server();
        server.startingServer();
    }
}