package client.consumers;

import shared.messages.file_transfer.FileTransferResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.TEAL;

public class FileTRespConsumer implements Consumer<String> {

    @Override
    public void accept(String JsonPayload) {
        try {
            FileTransferResp resp = JsonUtils.fromJson(JsonPayload, FileTransferResp.class);
            if ("OK".equalsIgnoreCase(resp.status())) {
                MessageHelper.printColoredMessage(TEAL, "Message sent!");
            } else {
                MessageHelper.handleErrorMessage(resp.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process BROADCAST_RESP message: " + e.getMessage());
        }
    }
}
