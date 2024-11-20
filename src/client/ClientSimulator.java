package client;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientSimulator {
    public static final String SERVER_ADDRESS = "13.95.168.137";
    public static final int SERVER_PORT = 1337;
    public static final int TOTAL_USERS = 1000000;

    public static void main(String[] args) {
        // Thread pool to manage client threads
        ExecutorService executorService = Executors.newFixedThreadPool(10000); // Limit to 1,000 threads for resource management

        for (int i = 0; i < TOTAL_USERS; i++) {
            final int userId = i + 1;
            executorService.execute(() -> connectToServer(userId));
        }

        executorService.shutdown();
    }

    /**
     * Simulates a single client connection to the server.
     *
     * @param userId the ID of the simulated user
     */
    public static void connectToServer(int userId) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("User " + userId + " connected to server.");

            // Simulate interaction with the server
            handleServerCommunication(socket, userId);

        } catch (IOException e) {
            System.err.println("User " + userId + " failed to connect: " + e.getMessage());
        }
    }

    /**
     * Handles communication between a simulated client and the server.
     *
     * @param socket the client's socket connection
     * @param userId the ID of the simulated user
     */
    private static void handleServerCommunication(Socket socket, int userId) {
        try {
            // Simulate sending and receiving messages
            socket.getOutputStream().write(("Hello from user " + userId).getBytes());
            socket.getOutputStream().flush();

            // Optionally, read a response (depends on your server implementation)
            byte[] buffer = new byte[1024];
            int bytesRead = socket.getInputStream().read(buffer);
            if (bytesRead > 0) {
                System.out.println("User " + userId + " received: " + new String(buffer, 0, bytesRead));
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("User " + userId + " communication error: " + e.getMessage());
        }
    }
}
