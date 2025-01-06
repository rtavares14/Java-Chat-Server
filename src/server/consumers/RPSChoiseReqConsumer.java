package server.consumers;

import server.clientHelper.ClientInstance;
import server.clientHelper.RPSHandler;
import shared.messages.RPSGame.play_game.GameChoiceReq;
import shared.messages.RPSGame.play_game.GameChoiceResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.OLIVE;
import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.*;

public class RPSChoiseReqConsumer implements Consumer<String> {
    private final ClientInstance clientInstance;


    public RPSChoiseReqConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void accept(String jsonPayload) {
        try {
            GameChoiceReq sendToReq = JsonUtils.fromJson(jsonPayload, GameChoiceReq.class);
            String choice = sendToReq.choice();

            if (clientInstance.getUsername() == null || clientInstance.getUsername().isEmpty()) {
                GameChoiceResp response = new GameChoiceResp("ERROR", 6000);
                clientInstance.sendCommand(RPS_CHOICE_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, RPS_CHOICE_RESP, JsonUtils.toJson(response));
            }

            //check if the player is in the game
            if (RPSHandler.getHandler().isPlayerInGame(clientInstance)) {
                if (RPSHandler.getHandler().didPlayerMakeChoice(clientInstance)) {
                    GameChoiceResp response = new GameChoiceResp("ERROR", 9005);
                    clientInstance.sendCommand(RPS_CHOICE_RESP, JsonUtils.toJson(response));
                    MessageHelper.printServerMessage(RED, clientInstance, RPS_CHOICE_RESP, JsonUtils.toJson(response));
                } else {

                    //check if the player choice is valid
                    if (choice.equals(ROCK.toString()) || choice.equals(PAPER.toString()) || choice.equals(SCISSORS.toString())) {
                        RPSHandler.getHandler().addChoice(clientInstance, choice);
                        GameChoiceResp response = new GameChoiceResp("OK", null);
                        clientInstance.sendCommand(RPS_CHOICE_RESP, JsonUtils.toJson(response));
                        MessageHelper.printServerMessage(OLIVE, clientInstance, RPS_CHOICE_RESP, JsonUtils.toJson(response));
                    }
                    //if the player choice is not valid
                    else {
                        GameChoiceResp response = new GameChoiceResp("ERROR", 9004);
                        clientInstance.sendCommand(RPS_CHOICE_RESP, JsonUtils.toJson(response));
                        MessageHelper.printServerMessage(RED, clientInstance, RPS_CHOICE_RESP, JsonUtils.toJson(response));
                    }
                }
            } else {
                GameChoiceResp response = new GameChoiceResp("ERROR", 9003);
                clientInstance.sendCommand(RPS_CHOICE_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, RPS_CHOICE_RESP, JsonUtils.toJson(response));
            }
        } catch (Exception e) {
            System.err.println("Failed to process RPS_CHOICE message: " + e.getMessage());
        }
    }
}
