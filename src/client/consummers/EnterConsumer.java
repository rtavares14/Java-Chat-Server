package client.consummers;

import shared.messages.BroadcastResp;
import shared.utils.JsonUtils;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;
import static shared.utils.MessageHandler.handleErrorMessage;

public class EnterConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            BroadcastResp message = JsonUtils.fromJson(json, BroadcastResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                System.out.println(GREEN + "Welcome to the chat!" + RESET);
            } else {
                handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process ENTER_RESP message: " + e.getMessage());
        }
    }
}