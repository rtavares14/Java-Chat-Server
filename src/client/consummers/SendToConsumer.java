package client.consummers;

import shared.messages.private_message.SendTo;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class SendToConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            SendTo message = JsonUtils.fromJson(json, SendTo.class);
            MessageHelper.printColoredMessage(WHITE, message.username() + " sent in private: " + message.message());
        } catch (Exception e) {
            System.err.println("Failed to process ENTER_RESP message: " + e.getMessage());
        }
    }
}
