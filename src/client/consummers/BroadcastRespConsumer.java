package client.consummers;

import shared.messages.BroadcastResp;
import shared.utils.JsonUtils;
import shared.utils.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class BroadcastRespConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            BroadcastResp message = JsonUtils.fromJson(json, BroadcastResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                MessageHelper.printColoredMessage(ORANGE , "Message sent!");
            } else {
                MessageHelper.handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process BROADCAST_RESP message: " + e.getMessage());
        }
    }
}