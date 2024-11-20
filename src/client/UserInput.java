package client;

import com.fasterxml.jackson.core.JsonProcessingException;
import shared.messages.Broadcast;
import shared.messages.Enter;
import shared.utils.JsonUtils;
import shared.enumerations.CmdColors;
import shared.enumerations.ServerCommands;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class UserInput implements Runnable {

    private String username;
    private Socket socket;
    private PrintWriter writer;
    private Scanner scanner = new Scanner(System.in);

    /**
     * Constructs a new UserInputHandler object.
     *
     * @param socket the socket.
     * @throws IOException if an I/O error occurs.
     */
    public UserInput(Socket socket) throws IOException {
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
        try {
            sleep(500);
            System.out.println(CmdColors.PURPLE + "Type 'help' to see available commands." + CmdColors.RESET);

            while (true) {
                String message = scanner.nextLine();

                if (message.toLowerCase().startsWith("login ")) {
                    userLogin(message);
                    sleep(150);
                    System.out.print(CmdColors.PURPLE + "Here is the list of commands you can use:" + CmdColors.RESET);
                    helperMenu();
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
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    /**
     * Option 1
     * Method to handle the login command
     *
     * @param message the message to be sent to the server
     */
    private void userLogin(String message) {
        //need to do the same style as i did in the sendGlobalMessage
        String userUsername = message.substring("login ".length());
        Enter enter = new Enter(userUsername);
        try {
            String json = JsonUtils.toJson(enter);
            writer.println(ServerCommands.LOGIN + " " + json);
        } catch (JsonProcessingException e) {
            System.err.println("Error creating JSON: " + e.getMessage());
        }
    }

    /**
     * Option 2
     * Method to handle the message command
     *
     * @param message the message to be sent to the server
     */
    private void sendGlobalMessage(String message) {
        String content = message.substring("msg ".length());
        Broadcast broadcast = new Broadcast(getUsername(), content);
        try {
            String json = JsonUtils.toJson(broadcast);
            writer.println(ServerCommands.BROADCAST.toString() + json);
        } catch (JsonProcessingException e) {
            System.err.println("Error creating JSON: " + e.getMessage());
        }
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
    public void logout() {
        // sends the logout to the server
        writer.println(ServerCommands.BYE.toString());
        username = null;
    }

    public String getUsername() {
        return username;
    }
}