package server.consumers;

import server.clientHelper.ClientInstance;
import server.clientHelper.RPSHandler;
import server.loggers.ClientLogger;
import server.loggers.ServerLogger;
import shared.messages.general.Info;
import shared.messages.RPSGame.enter_game.GameMsg;
import shared.messages.RPSGame.enter_game.GameStartReq;
import shared.messages.private_message.SendToResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.OLIVE;
import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.*;

public class RPSStartReqConsumer implements Consumer<String> {
    private final ClientInstance clientInstance;

    public RPSStartReqConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }


    @Override
    public void accept(String jsonPayload) {
        try {
            GameStartReq gameStartReq = JsonUtils.fromJson(jsonPayload, GameStartReq.class);
            String player2 = gameStartReq.getPlayer2();

            if (clientInstance.getUsername() == "" || clientInstance.getUsername().isEmpty()) {
                SendToResp response = new SendToResp("ERROR", 6000);
                clientInstance.sendCommand(RPS_START_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, RPS_START_RESP, JsonUtils.toJson(response));
                return;
            }

            ClientInstance receiverInstance = ClientLogger.getInstance().getClient(player2);
            if (receiverInstance == null) {
                SendToResp response = new SendToResp("ERROR", 6006);
                clientInstance.sendCommand(RPS_START_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, RPS_START_RESP, JsonUtils.toJson(response));
            } else if (receiverInstance.getUsername().equals(clientInstance.getUsername())) {
                SendToResp response = new SendToResp("ERROR", 9009);
                clientInstance.sendCommand(RPS_START_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, RPS_START_RESP, JsonUtils.toJson(response));
            } else if (RPSHandler.getHandler().isGameInProgress()) {
                SendToResp response = new SendToResp("ERROR", 9001);
                clientInstance.sendCommand(RPS_START_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, RPS_START_RESP, JsonUtils.toJson(response));
            } else {
                // Send confirmation to the player1
                SendToResp response = new SendToResp("OK", null);
                clientInstance.sendCommand(RPS_START_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(OLIVE, clientInstance, RPS_START_RESP, JsonUtils.toJson(response));

                // Send the message to the player2
                GameMsg gameMsg = new GameMsg(clientInstance.getUsername());
                String json = JsonUtils.toJson(gameMsg);
                receiverInstance.sendCommand(RPS_MSG, json);
                MessageHelper.printServerMessage(OLIVE, ClientLogger.getInstance().getClient(player2), RPS_MSG, JsonUtils.toJson(response));

                RPSHandler.getHandler().addPlayers(clientInstance, receiverInstance);

                // Inform all users
                Info info = new Info("A Rock Paper Scissors game has been started by " + clientInstance.getUsername() + " and " + player2);
                ServerLogger.getInstance().informAllUsers(info, INFO, clientInstance, receiverInstance);


            }
        } catch (Exception e) {
            System.err.println("Failed to process SENDTO_REQ message: " + e.getMessage());
        }

    }
}
