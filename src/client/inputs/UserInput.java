package client.inputs;

import com.fasterxml.jackson.core.JsonProcessingException;
import shared.messages.*;
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
                String command = message.split(" ")[0].toLowerCase();

                switch (command) {
                    case "login":
                        userLogin(message);
                        break;
                    case "msg":
                        sendGlobalMessage(message);
                        break;
                    case "pvm":
                        sendPrivateMessage(message);
                        break;
                    case "ulist":
                        requestUserList();
                        break;
                    case "help":
                        helperMenu();
                        break;
                    case "bye":
                        logout();
                        return;
                    default:
                        MessageHelper.printColoredMessage(CmdColors.RED, "Invalid command. Please try again.");
                        helperMenu();
                        break;
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
     * Method to handle the list command
     *
     */
    private void requestUserList() {
        writer.println(LIST_REQ);
    }

    /**
     * Option 3
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
     * Option 4
     * Method to handle the private message command
     *
     * @param message the message to be sent to the server
     */
    private void sendPrivateMessage(String message) throws JsonProcessingException {
        String[] parts = message.split(" ", 3);
        if (parts.length < 3) {
            MessageHelper.printColoredMessage(CmdColors.RED, "To send a private message type pvm <username> <message>");
        } else {
            String receiver = parts[1];
            String content = parts[2];
            SendToReq sendTo = new SendToReq(receiver, content);
            String json = JsonUtils.toJson(sendTo);
            sendCommand(SENDTO_REQ, json);
        }
    }

    /**
     * Helper menu for the user
     */
    private void helperMenu() {
        System.out.println(CmdColors.PURPLE + "Commands:");
        System.out.println("login \"username\" - Login to the server");
        System.out.println("ulist - Request a list of all login users");
        System.out.println("msg \"message\" - Send a global broadcast message");
        System.out.println("pvm \"username\" \"message\" - Send a private message to a user");
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