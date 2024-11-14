package utils;

import com.fasterxml.jackson.databind.JsonNode;
import utils.enumerations.CmdColors;

import java.io.PrintWriter;

public class MessageHandler {

    /**
     * Determine the message based on the server message
     * @param serverMessage the server message
     * @return the message
     */
    public static String determineMessage(ServerMessage serverMessage, PrintWriter writer) {
        JsonNode data = serverMessage.getData();
        if (data == null && "PING".equals(serverMessage.getType())) {
            //here i need to send pong back to server to keep connection alive
            //not sending pong as an output but as a response to server
            writer.println("PONG");
            return CmdColors.PURPLE + "PONG send" + CmdColors.RESET;
        } else
        if (data != null && "READY".equals(serverMessage.getType())) {
            return CmdColors.PURPLE + "Ready to chat" + CmdColors.RESET;
        }
        else if (data != null && "BROADCAST".equals(serverMessage.getType())) {
            return handleBroadcastMessage(data);
        } else if (data != null && "LEFT".equals(serverMessage.getType())) {
            return handleLeftMessage(data);
        }
        else if (data != null && data.has("status") && "OK".equals(data.get("status").asText())) {
            return switch (serverMessage.getType()) {
                case "ENTER_RESP" -> CmdColors.GREEN + "Chat entered" + CmdColors.RESET;
                case "BROADCAST_RESP" -> CmdColors.ORANGE + "Message sent" + CmdColors.RESET;
                case "PING" -> "Ping";
                case "HANGUP" -> "Hangup";
                case "BYE_RESP" -> CmdColors.PURPLE + "Bye see you later" + CmdColors.RESET;
                default -> "Unknown command";
            };
        } else {
            return determineErrorMessage(serverMessage);
        }
    }

    /**
     * Handle the left message
     * @param data the data
     * @return the message
     */
    private static String handleLeftMessage(JsonNode data) {
        if (data.has("username")) {
            String username = data.get("username").asText();
            if (username.isEmpty()) {
                return CmdColors.RED + "Someone left the chat" + CmdColors.RESET;
            }
            else{
            return CmdColors.RED + username + " left the chat" + CmdColors.RESET;
            }
        } else {
            return "Invalid left message format";
        }
    }

    /**
     * Handle the broadcast message
     * @param data the data
     * @return the message
     */
    private static String handleBroadcastMessage(JsonNode data) {
        if (data.has("username") && data.has("message")) {
            String username = data.get("username").asText();
            String message = data.get("message").asText();
            return CmdColors.ORANGE + username + ": " + message + CmdColors.RESET;
        } else {
            return "Invalid broadcast message format";
        }
    }


    /**
     * Determine the error message based on the server message
     * @param serverMessage the server message
     * @return the error message
     */
    private static String determineErrorMessage(ServerMessage serverMessage) {
        JsonNode data = serverMessage.getData();
        if (data != null && data.has("code")) {
            return switch (data.get("code").asText()) {
                case "5000" -> CmdColors.RED + "User with this name already exists" + CmdColors.RESET;
                case "5001" -> CmdColors.RED + "Username has an invalid format or length" + CmdColors.RESET;
                case "5002" -> CmdColors.RED + "Already logged in" + CmdColors.RESET;
                case "6000" -> CmdColors.RED + "User is not logged in" + CmdColors.RESET;
                case "7000" -> CmdColors.RED + "No pong received" + CmdColors.RESET;
                case "8000" -> CmdColors.RED + "Server error" + CmdColors.RESET;
                default -> "Unknown error " + data.get("code").asText() + " occurred - " + serverMessage.getType();
            };
        } else {
            return "Unknown error occurred - " + serverMessage.getType();
        }
    }
}