package client.consummers;

import shared.messages.Broadcast;
import shared.utils.JsonUtils;
import shared.utils.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class BroadcastConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            Broadcast message = JsonUtils.fromJson(json, Broadcast.class);
            MessageHelper.printColoredMessage(ORANGE, message.username() + " sent: " + message.message());
        } catch (Exception e) {
            System.err.println("Failed to process BROADCAST message: " + e.getMessage());
        }
    }
}