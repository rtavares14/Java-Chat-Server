package client.inputs;

import com.fasterxml.jackson.core.JsonProcessingException;
import shared.messages.BroadcastReq;
import shared.messages.Enter;
import shared.utils.JsonUtils;
import shared.enumerations.CmdColors;
import shared.enumerations.ServerCommands;
import shared.utils.MessageHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import static shared.enumerations.ServerCommands.*;

public class UserInput implements Runnable {

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
                    MessageHelper.printColoredMessage(CmdColors.RED, "Invalid command. Please try again.");
                    helperMenu();
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
    private void userLogin(String message) throws JsonProcessingException {
        String userUsername = message.substring("login ".length());
        Enter enter = new Enter(userUsername);
        String json = JsonUtils.toJson(enter);
        sendCommand(LOGIN, json);
    }

    /**
     * Option 2
     * Method to handle the message command
     *
     * @param message the message to be sent to the server
     */
    private void sendGlobalMessage(String message) throws JsonProcessingException {
        String content = message.substring("msg ".length());
        BroadcastReq broadcast = new BroadcastReq(content);
        String json = JsonUtils.toJson(broadcast);
        sendCommand(BROADCAST_REQ, json);
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
        writer.println(BYE);
    }

    /**
     * Helper method to send server commands with JSON payloads
     *
     * @param command     the server command to be sent
     * @param jsonPayload the JSON payload associated with the command
     */
    private void sendCommand(ServerCommands command, String jsonPayload) {
        if (command != null && jsonPayload != null && !jsonPayload.isEmpty()) {
            writer.println(command + " " + jsonPayload);
        } else {
            System.err.println("Invalid command or payload. Cannot send to server.");
        }
    }
}