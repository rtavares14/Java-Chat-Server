package client.consumers;

import shared.messages.RPSGame.enter_game.GameStartResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.OLIVE;

public class RPSStartRespConsumer implements Consumer<String> {

    @Override
    public void accept(String jsonPayload) {
        try {
            GameStartResp message = JsonUtils.fromJson(jsonPayload, GameStartResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                MessageHelper.printColoredMessage(OLIVE, "Invite sent!");
            } else {
                MessageHelper.handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process RPS_START_RESP message: " + e.getMessage());
        }
    }
}
