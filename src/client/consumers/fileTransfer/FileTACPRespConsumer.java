package client.consumers.fileTransfer;

import shared.messages.broadcast.BroadcastResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.TEAL;

public class FileTACPRespConsumer implements Consumer<String> {


    @Override
    public void accept(String json) {
        try {
            BroadcastResp message = JsonUtils.fromJson(json, BroadcastResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                MessageHelper.printColoredMessage(TEAL, "File transfer will start as soon as possible.");
            } else {
                MessageHelper.handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process FILET_ACP_RESP message: " + e.getMessage());
        }
    }
}
