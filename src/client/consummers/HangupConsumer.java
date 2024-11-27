package client.consummers;

import shared.utils.MessageWriter;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class HangupConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        MessageWriter.printColoredMessage(RED, "Bye bye see you later!");
    }
}