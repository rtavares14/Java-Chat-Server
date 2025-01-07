package client.consumers;

import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.RED;

public class HangupConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        MessageHelper.printColoredMessage(RED, "You have been hung up on.");
    }
}