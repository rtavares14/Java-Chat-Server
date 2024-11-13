package client;

import utils.enumerations.CmdColors;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class UserInputHandler implements Runnable {

    private Socket socket;
    private String username;
    private PrintWriter writer;

    public UserInputHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.username = null;
        OutputStream out = socket.getOutputStream();
        this.writer = new PrintWriter(out, true);
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String message = scanner.nextLine();

            if (message.toLowerCase().startsWith("login ")) {
                userLogin(message);
            }

            else if (message.toLowerCase().startsWith("msg ")) {
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
            }

            else {
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
        System.out.println("Commands:");
        System.out.println("login \"username\" - Login to the server");
        System.out.println("msg \"message\" - Send a global broadcast message");
        System.out.println("help - Display this help menu");
        System.out.println("bye - Disconnect from the server");
    }

    /**
     * Method to handle the logout command
     */
    private void logout() {
        if (username == null) {
            System.out.println("You are not logged in.");
            return;
        }

        // sends the logout to the server
        writer.println("BYE");
        System.out.println(CmdColors.RED + "Goodbye " + username + "!" + CmdColors.RESET);
        username = null;
    }
}