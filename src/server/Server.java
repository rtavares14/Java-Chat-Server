package server;

import server.clientInstance.ClientInstance;
import server.handlers.RPSHandler;
import server.loggers.ClientLogger;
import shared.utils.messages.MessageHelper;

import java.net.ServerSocket;
import java.net.Socket;

import static shared.enumerations.CmdColors.*;

public class Server {

    private final int SERVER_PORT = 1337;
    private final int FILE_PORT = 1338;
    private final String VERSION = "RCT Chat Server V1.14";
    private ServerSocket serverSocket;
    private ServerSocket fileServerSocket;

    /**
     * Main method
     * This method is used to start the server
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.startingServer();
    }

    /**
     * Get version
     * This method is used to get the version of the server
     *
     * @return String
     */
    public String getVersion() {
        return VERSION;
    }

    /**
     * Start the server
     * This method is used to start the server
     */
    public void startingServer() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            fileServerSocket = new ServerSocket(FILE_PORT);
            MessageHelper.printColoredMessage(PURPLE, "Starting server version (" + VERSION + ") on port: " + SERVER_PORT);
            MessageHelper.printColoredMessage(PURPLE, "Starting file server on port: " + FILE_PORT);

            // Initialize the RPS game handler
            RPSHandler.getInstance().startGameRoom();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                MessageHelper.printColoredMessage(PURPLE, "New client connected: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                ClientInstance clientInstance = new ClientInstance(clientSocket, this);
                ClientLogger.getInstance().addClient(clientInstance);
                new Thread(clientInstance).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    /**
     * Stop the server
     * This method is used to stop the server
     */
    public void stopServer() {
        try {
            MessageHelper.printColoredMessage(PURPLE, "Stopping server...");
            serverSocket.close();
            fileServerSocket.close();

            // Stop the RPSHandler
            RPSHandler.getInstance().stopGameRoom();

            ClientLogger.getInstance().closeAllClients();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}