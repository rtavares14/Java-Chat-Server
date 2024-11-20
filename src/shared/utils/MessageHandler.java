package shared.utils;

import com.fasterxml.jackson.databind.JsonNode;
import shared.enumerations.CmdColors;
import shared.enumerations.ServerCommands;

import java.io.PrintWriter;

public class MessageHandler {

    /**
     * Determine the message
     *
     * @param serverMessage the server message
     * @param writer the writer
     */
    public static void determineMessage(ServerMessage serverMessage, PrintWriter writer) {
        JsonNode data = serverMessage.getData();
        if (serverMessage.getType() == ServerCommands.PING) {
            writer.println("PONG");
            //System.out.println(CmdColors.PURPLE + "PONG sent" + CmdColors.RESET);
        } else if (serverMessage.getType() == ServerCommands.READY) {
            System.out.println(CmdColors.PURPLE + "Ready to chat" + CmdColors.RESET);
        } else if (serverMessage.getType() == ServerCommands.BROADCAST) {
            handleBroadcastMessage(data);
        } else if (serverMessage.getType() == ServerCommands.LEFT) {
            handleLeftMessage(data);
        } else if (data.has("status") && "OK".equals(data.get("status").asText())) {
            switch (serverMessage.getType()) {
                case ServerCommands.ENTER_RESP -> System.out.println(CmdColors.GREEN + "Chat entered" + CmdColors.RESET);
                case ServerCommands.BROADCAST_RESP -> System.out.println(CmdColors.ORANGE + "Message sent" + CmdColors.RESET);
                case ServerCommands.PING -> System.out.println("Ping");
                case ServerCommands.HANGUP -> System.out.println("Hangup");
                case ServerCommands.BYE_RESP -> System.out.println(CmdColors.PURPLE + "Bye see you later" + CmdColors.RESET);
                default -> System.out.println("Unknown command");
            }
        } else {
            determineErrorMessage(serverMessage);
        }
    }

    /**
     * Handle the left message
     *
     * @param data the data
     */
    private static void handleLeftMessage(JsonNode data) {
        if (data.has("username")) {
            String username = data.get("username").asText();
            if (username.isEmpty()) {
                System.out.println(CmdColors.RED + "Someone left the chat" + CmdColors.RESET);
            } else {
                System.out.println(CmdColors.RED + username + " left the chat" + CmdColors.RESET);
            }
        } else {
            System.out.println("Invalid left message format");
        }
    }

    /**
     * Handle the broadcast message
     *
     * @param data the data
     */
    private static void handleBroadcastMessage(JsonNode data) {
        if (data.has("username") && data.has("message")) {
            String username = data.get("username").asText();
            String message = data.get("message").asText();
            System.out.println(CmdColors.ORANGE + username + ": " + message + CmdColors.RESET);
        } else {
            System.out.println("Invalid broadcast message format");
        }
    }

    /**
     * Determine the error message
     *
     * @param serverMessage the server message
     */
    private static void determineErrorMessage(ServerMessage serverMessage) {
        JsonNode data = serverMessage.getData();
        if (data != null && data.has("code")) {
            switch (data.get("code").intValue()) {
                case 5000 -> System.out.println(CmdColors.RED + "User with this name already exists" + CmdColors.RESET);
                case 5001 -> System.out.println(CmdColors.RED + "Username has an invalid format or length" + CmdColors.RESET);
                case 5002 -> System.out.println(CmdColors.RED + "Already logged in" + CmdColors.RESET);
                case 6000 -> System.out.println(CmdColors.RED + "User is not logged in" + CmdColors.RESET);
                case 7000 -> System.out.println(CmdColors.RED + "No pong received" + CmdColors.RESET);
                case 8000 -> System.out.println(CmdColors.RED + "Server error" + CmdColors.RESET);
                default -> System.out.println("Unknown error " + data.get("code").asText() + " occurred - " + serverMessage.getType());
            }
        } else {
            System.out.println("Unknown error occurred - " + serverMessage.getType());
        }
    }
}