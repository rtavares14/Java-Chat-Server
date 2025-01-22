package client.consumers;

import shared.messages.file_transfer.status.FileTransferRejected;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class FileTRejConsumer implements Consumer<String> {

    @Override
    public void accept(String jsonPayload) {
        try {
            FileTransferRejected message = JsonUtils.fromJson(jsonPayload, FileTransferRejected.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                MessageHelper.printColoredMessage(RED, "File was rejected by the receiver");
            }
        } catch (Exception e) {
            System.err.println("Failed to process FILET_REJECTED message: " + e.getMessage());
        }
    }
}
