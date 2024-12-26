package client.consumers;

import shared.messages.RPSGame.play_game.GameChoiceResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.OLIVE;

public class RPSChoiceRespConsumer implements Consumer<String> {

    @Override
    public void accept(String jsonPayload) {
        try {
            GameChoiceResp message = JsonUtils.fromJson(jsonPayload, GameChoiceResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                MessageHelper.printColoredMessage(OLIVE, "Message sent!");
            } else {
                MessageHelper.handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process BROADCAST_RESP message: " + e.getMessage());
        }

    }
}
