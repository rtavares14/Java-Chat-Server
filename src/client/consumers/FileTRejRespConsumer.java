package client.consumers;

import shared.messages.broadcast.BroadcastResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.TEAL;

public class FileTRejRespConsumer implements Consumer<String> {


    @Override
    public void accept(String json) {
        try {
            BroadcastResp message = JsonUtils.fromJson(json, BroadcastResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                MessageHelper.printColoredMessage(TEAL, "File transfer was rejected");
            } else {
                MessageHelper.handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process FILET_REJ_RESP message: " + e.getMessage());
        }
    }
}
