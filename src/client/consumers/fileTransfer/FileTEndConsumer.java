package client.consumers.fileTransfer;

import shared.messages.file_transfer.status.FileTransferEnd;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.TEAL;

public class FileTEndConsumer implements Consumer<String> {


    @Override
    public void accept(String json) {
        try {
            FileTransferEnd message = JsonUtils.fromJson(json, FileTransferEnd.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                MessageHelper.printColoredMessage(TEAL, "FILE TRANSFER SUCCESSFUL");
            } else {
                MessageHelper.handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process FILET_END message: " + e.getMessage());
        }
    }
}
