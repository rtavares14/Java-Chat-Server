package client.consumers;

import shared.messages.Ready;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.PURPLE;

public class ReadyConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            Ready message = JsonUtils.fromJson(json, Ready.class);
            MessageHelper.printColoredMessage(PURPLE, "Server is ready. Version: " + message.version());
            MessageHelper.printColoredMessage(PURPLE, "Type 'help' to see available commands");
            MessageHelper.printColoredMessage(PURPLE, "Type 'login <username>' to enter the chat");
        } catch (Exception e) {
            System.err.println("Failed to process READY message: " + e.getMessage());
        }
    }
}
