package server.consumers.login_logout;

import server.clientInstance.ClientInstance;
import server.handlers.HeartbeatHandler;
import server.loggers.ClientLogger;
import server.loggers.ServerLogger;
import shared.messages.enter.Enter;
import shared.messages.enter.EnterResp;
import shared.messages.login_logout.Joined;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.GREEN;
import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.ENTER_RESP;
import static shared.enumerations.ServerCommands.JOINED;

public class EnterConsumer implements Consumer<String> {
    private final ClientInstance clientInstance;


    public EnterConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void accept(String jsonPayload) {
        try {
            Enter enter = JsonUtils.fromJson(jsonPayload, Enter.class);
            EnterResp response = handleLogin(enter, clientInstance);
            clientInstance.sendCommand(ENTER_RESP, JsonUtils.toJson(response));

            if (response.getStatus().equals("OK")) {
                MessageHelper.printServerMessage(GREEN, clientInstance, ENTER_RESP, JsonUtils.toJson(response));
                Joined joined = new Joined(clientInstance.getUsername());
                ServerLogger.getInstance().getClientUserCounts();
                ServerLogger.getInstance().broadcastMessage(joined, clientInstance.getUsername(), JOINED);
                HeartbeatHandler.getInstance().startHeartbeat(clientInstance);
            } else {
                MessageHelper.printServerMessage(RED, clientInstance, ENTER_RESP, JsonUtils.toJson(response));
            }
        } catch (Exception e) {
            try {
                EnterResp response = new EnterResp("ERROR", 5001);
                clientInstance.sendCommand(ENTER_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, ENTER_RESP, JsonUtils.toJson(response));
            } catch (Exception ex) {
                System.err.println("Failed to process ENTER message: " + e.getMessage());
            }
        }
    }

    /**
     * Handle the login message
     * This method is used to handle the login message
     * It checks if the username is valid and available and then logs the user in
     *
     * @param loginMessage the login message
     * @return the response to the login message
     */
    private EnterResp handleLogin(Enter loginMessage, ClientInstance clientInstance) {
        String username = loginMessage.getUsername();

        if (clientInstance.getUsername() != null && !clientInstance.getUsername().isEmpty()) {
            return new EnterResp("ERROR", 5002);
        }

        if (ClientLogger.isUsernameValid(username) && ClientLogger.isUsernameAvailable(username) &&
                clientInstance.getUsername().equals("")) {
            ClientLogger.getInstance().logInUser(username, clientInstance);
            clientInstance.setUsername(username);
            return new EnterResp("OK", null);
        } else {
            int errorCode = ClientLogger.checkUsername(username);
            return new EnterResp("ERROR", errorCode);
        }
    }
}