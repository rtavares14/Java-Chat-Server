package server.loggers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.clientHelper.ClientInstance;
import shared.enumerations.ServerCommands;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import static shared.enumerations.CmdColors.*;
import static shared.enumerations.ServerCommands.JOINED;
import static shared.enumerations.ServerCommands.LEFT;

public class ServerLogger {

    private static ServerLogger instance;

    /**
     * Constructor for the ServerLogger class
     */
    private ServerLogger() {
    }

    /**
     * Get instance
     * This method is used to get the instance of the server logger
     *
     * @return ServerLogger
     */
    public static ServerLogger getInstance() {
        if (instance == null) {
            instance = new ServerLogger();
        }
        return instance;
    }

    /**
     * Get client user counts
     * This method is used to get the client user counts
     */
    public void getClientUserCounts() {
        MessageHelper.printColoredMessage(GREEN, ClientLogger.getInstance().getLoggedInUsers().size() + " client(s) / " + ClientLogger.getInstance().getAllClients().size() + " user(s)");
    }

    public void informAllUsers(Object message, ServerCommands command,ClientInstance player1, ClientInstance player2) throws JsonProcessingException {

        String jsonMessage = JsonUtils.toJson(message);
        for (ClientInstance client : ClientLogger.getInstance().getLoggedInUsers().values()) {
            if (!client.equals(player1) && !client.equals(player2)) {
                client.sendCommand(command, jsonMessage);
            }
        }

        MessageHelper.printColoredMessage(OLIVE, "S --> (ALL): " + command + " " + jsonMessage);
    }

    /**
     * Broadcast message
     * This method is used to broadcast a message to all users
     *
     * @param message        the message to be broadcasted
     * @param senderUsername the username of the sender
     * @param command        the command
     * @throws JsonProcessingException if an exception occurs
     */
    public void broadcastMessage(Object message, String senderUsername, ServerCommands command) throws JsonProcessingException {
        String jsonMessage = JsonUtils.toJson(message);
        for (ClientInstance client : ClientLogger.getInstance().getLoggedInUsers().values()) {
            if (!client.getUsername().equals(senderUsername)) {
                client.sendCommand(command, jsonMessage);
            }
        }
        if (command == LEFT) {
            MessageHelper.printColoredMessage(YELLOW, "S --> (ALL): " + command + " " + jsonMessage);
        } else if (command == JOINED) {
            MessageHelper.printColoredMessage(BLUE, "S --> (ALL): " + command + " " + jsonMessage);
        } else {
            MessageHelper.printColoredMessage(ORANGE, "S --> (ALL): " + command + " " + jsonMessage);
        }
    }
}
