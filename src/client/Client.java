package client;

import shared.enumerations.CmdColors;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public static final String SERVER_ADDRESS = "127.0.0.1";
    public static final int SERVER_PORT = 1337;

    /**
     * Main method to start the client.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        new Client().run();
    }

    /**
     * Connects to the server and starts a new thread to handle user input and server messages.
     * If the connection is lost, it will try to reconnect every 5 seconds.
     * If the connection is successful, it will break the loop.
     * If the client is shutting down, it will log out the user.
     */
    public void run() {
        while (true) {
            try {
                //connection to the server
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                System.out.println(CmdColors.PURPLE + "Connected to server." + CmdColors.RESET);

                // start a new thread to handle user input and server messages
                UserInput userInputHandler = new UserInput(socket);
                ServerInput serverInput = new ServerInput(socket);
                Thread serverThread = new Thread(serverInput);
                Thread clientThread = new Thread(userInputHandler);

                serverThread.start();
                clientThread.start();


                //The shutdown hook is a thread that is executed when the JVM is shutting down.
                //  It is used to perform cleanup operations before the JVM shuts down.
                // The shutdown hook is a good way to perform cleanup operations before the JVM shuts down.
                // https://www.baeldung.com/jvm-shutdown-hooks
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println(CmdColors.PURPLE + "Client is shutting down..." + CmdColors.RESET);
                    userInputHandler.logout();
                }));

                // break the loop if connection is successful
                break;

            } catch (IOException e) {
                System.err.println("Could not connect to server: HAHAHA AGAIN");
                // stops for 5 seconds before trying again
                // I chose 5 seconds because it is a good amount of time to wait before trying to reconnect
                // I chose this implementation because it is a good way to handle the connection to the server
                // Also we need a heartbeat connection in Parallel computing in Quartile 1
                try {
                    // stops for 5 seconds before trying again
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    System.err.println("Sleep interrupted: " + ie.getMessage());
                }
            }
        }
    }
}