package client.consummers;

import shared.messages.Ready;
import shared.utils.JsonUtils;
import shared.utils.MessageWriter;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class ReadyConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            Ready message = JsonUtils.fromJson(json, Ready.class);
            MessageWriter.printColoredMessage(PURPLE, "Server is ready. Version: " + message.version());
            MessageWriter.printColoredMessage(PURPLE, "Type 'help' to see available commands");
            MessageWriter.printColoredMessage(PURPLE, "Type 'login <username>' to enter the chat");
        } catch (Exception e) {
            System.err.println("Failed to process READY message: " + e.getMessage());
        }
    }
}
