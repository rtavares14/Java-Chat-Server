package client.consumers;

import shared.messages.private_message.SendToResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class ListRespConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            SendToResp message = JsonUtils.fromJson(json, SendToResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                MessageHelper.printColoredMessage(CYAN , "List response!");
            } else {
                MessageHelper.handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process LIST_RESP message: " + e.getMessage());
        }
    }
}
