package client.consumers.rps;

import shared.messages.RPSGame.enter_game.GameStartResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.OLIVE;

public class RPSStartRespConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            GameStartResp message = JsonUtils.fromJson(json, GameStartResp.class);
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
