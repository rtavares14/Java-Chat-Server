package client.consumers.rps;

import shared.messages.RPSGame.play_game.GameEnd;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.OLIVE;

public class RPSEndConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            GameEnd message = JsonUtils.fromJson(json, GameEnd.class);
            if (message.winner() == null) {
                MessageHelper.printColoredMessage(OLIVE, "The game is a tie! Both players chose " + message.choiceC1());
            } else {
                MessageHelper.printColoredMessage(OLIVE, message.winner() + " has won the game! " + message.player1() + " chose " + message.choiceC1() + " and " + message.player2() + " chose " + message.choiceC2());
            }
        } catch (Exception e) {
            System.err.println("Failed to process ENTER_RESP message: " + e.getMessage());
        }
    }
}
