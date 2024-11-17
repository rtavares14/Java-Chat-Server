package client;

import utils.enumerations.CmdColors;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class UserInputHandler implements Runnable {

    private String username;
    private Socket socket;
    private PrintWriter writer;
    private Scanner scanner;

    /**
     * Constructs a new UserInputHandler object.
     *
     * @param socket the socket.
     * @throws IOException if an I/O error occurs.
     */
    public UserInputHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.username = null;
        OutputStream out = socket.getOutputStream();
        this.writer = new PrintWriter(out, true);
    }

    /**
     * Receives user input and sends it to the server.
     * The user can enter the following commands:
     * - login "username" - Login to the server
     * - msg "message" - Send a global broadcast message
     * - help - Display the help menu
     * - bye - Disconnect from the server
     */
    @Override
    public void run() {
        while (true) {
            String message = scanner.nextLine();

            if (message.toLowerCase().startsWith("login ")) {
                userLogin(message);
            } else if (message.toLowerCase().startsWith("msg ")) {
                sendGlobalMessage(message);
            }

            // Helper menu for acoustic people who do not know the commands
            // I am the acoustic person, so I need this :(
            else if (message.equalsIgnoreCase("help")) {
                helperMenu();
            }

            // Paka Paka condition
            else if (message.equalsIgnoreCase("bye")) {
                logout();
                break;
            } else {
                System.out.println(CmdColors.RED + "Invalid command. Please try again." + CmdColors.RESET);
            }
        }
    }


    /**
     * Option 1
     * Method to handle the login command
     *
     * @param message the message to be sent to the server
     */
    private void userLogin(String message) {
        String[] parts = message.split(" ");
        if (parts.length != 2) {
            System.out.println("Invalid login command. Please try again.");
            return;
        }

        String username = parts[1];
        this.username = username;
        // sends the login to the server
        writer.println("ENTER {\"username\":\"" + username + "\"}");
    }

    /**
     * Option 2
     * Method to handle the message command
     *
     * @param message the message to be sent to the server
     */
    private void sendGlobalMessage(String message) {
        String[] parts = message.split(" ");
        if (parts.length < 1) {
            System.out.println("Invalid message command. Please try again.");
            return;
        }

        //get all parts from the message except the 1 part
        message = message.substring(parts[0].length() + 1);
        // sends the message to the server
        writer.println("BROADCAST_REQ {\"message\":\"" + message + "\"}");
    }

    /**
     * Helper menu for the user
     */
    private void helperMenu() {
        System.out.println(CmdColors.PURPLE + "Commands:");
        System.out.println("login \"username\" - Login to the server");
        System.out.println("msg \"message\" - Send a global broadcast message");
        System.out.println("help - Display this help menu");
        System.out.println("bye - Disconnect from the server" + CmdColors.RESET);
    }

    /**
     * Method to handle the logout command
     */
    private void logout() {
        // sends the logout to the server
        writer.println("BYE");
        username = null;
    }

    private void closeResources() {
        try {
            if (scanner != null) scanner.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
}