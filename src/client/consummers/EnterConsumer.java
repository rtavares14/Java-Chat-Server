package client.consummers;

import shared.messages.BroadcastResp;
import shared.utils.JsonUtils;
import shared.utils.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class EnterConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            BroadcastResp message = JsonUtils.fromJson(json, BroadcastResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                MessageHelper.printColoredMessage(GREEN, "Welcome to the chat!");
            } else {
                MessageHelper.handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process ENTER_RESP message: " + e.getMessage());
        }
    }
}