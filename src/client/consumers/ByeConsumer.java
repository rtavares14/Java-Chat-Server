package client.consumers;

import shared.messages.broadcast.BroadcastResp;
import shared.messages.login_logout.ByeResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class ByeConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            ByeResp message = JsonUtils.fromJson(json, ByeResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                MessageHelper.printColoredMessage(PURPLE, "Bye bye see you later!");
            }
        } catch (Exception e) {
            System.err.println("Failed to process BYE_RESP message: " + e.getMessage());
        }
    }
}