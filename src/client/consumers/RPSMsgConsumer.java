package client.consumers;

import shared.messages.RPSGame.enter_game.GameMsg;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.OLIVE;

public class RPSMsgConsumer implements Consumer<String> {

    @Override
    public void accept(String jsonPayload) {
        try {
            GameMsg message = JsonUtils.fromJson(jsonPayload, GameMsg.class);
            MessageHelper.printColoredMessage(OLIVE, message.getPlayer1() + " wants to play with you!");

        } catch (Exception e) {
            System.err.println("Failed to process ENTER_RESP message: " + e.getMessage());
        }
    }
}
