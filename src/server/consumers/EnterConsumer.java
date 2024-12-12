package server.consumers;

import server.clientHelper.LoginHandler;
import server.clientHelper.ClientInstance;
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
    private final LoginHandler loginHandler;


    public EnterConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
        this.loginHandler = new LoginHandler(clientInstance);
    }

    @Override
    public void accept(String jsonPayload) {
        try {
            Enter enter = JsonUtils.fromJson(jsonPayload, Enter.class);
            EnterResp response = loginHandler.handleLogin(enter);
            clientInstance.sendCommand(ENTER_RESP, JsonUtils.toJson(response));

            if (response.getStatus().equals("OK")) {
                MessageHelper.printColoredMessage(GREEN, "S --> (" + clientInstance.getUsername() + "): " + JsonUtils.toJson(response));
                Joined joined = new Joined(clientInstance.getUsername());
                ServerLogger.getInstance().broadcastMessage(joined, clientInstance.getUsername(), JOINED);
                ServerLogger.getInstance().getClientUserCounts();
            } else {
                MessageHelper.printColoredMessage(RED, "S --> (): " + JsonUtils.toJson(response));
            }
        } catch (Exception e) {
            try {
                EnterResp response = new EnterResp("ERROR", 5001);
                clientInstance.sendCommand(ENTER_RESP, JsonUtils.toJson(response));
                MessageHelper.printColoredMessage(RED, "S --> (): " + JsonUtils.toJson(response));
            } catch (Exception ex) {
                System.err.println("Failed to process ENTER message: " + e.getMessage());
            }
        }
    }
}