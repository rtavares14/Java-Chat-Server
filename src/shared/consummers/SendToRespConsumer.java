package shared.consummers;

import shared.messages.private_message.SendToResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class SendToRespConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            SendToResp message = JsonUtils.fromJson(json, SendToResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                MessageHelper.printColoredMessage(WHITE , "Private message sent!");
            } else {
                MessageHelper.handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process ENTER_RESP message: " + e.getMessage());
        }
    }
}
