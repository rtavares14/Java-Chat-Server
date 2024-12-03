package client.consummers;

import shared.utils.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class HangupConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        MessageHelper.printColoredMessage(RED, "Bye bye see you later!");
    }
}