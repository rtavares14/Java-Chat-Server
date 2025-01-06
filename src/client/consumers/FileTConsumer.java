package client.consumers;

import shared.messages.file_transfer.FileTransfer;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.TEAL;

public class FileTConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            FileTransfer message = JsonUtils.fromJson(json, FileTransfer.class);
            String formattedSize = String.format("%.2f", message.size());
            MessageHelper.printColoredMessage(TEAL, message.sender() + " wants to send you " + message.getFileNameFromPath() + " with size " + formattedSize + " KB");
        } catch (Exception e) {
            System.err.println("Failed to process FILET message: " + e.getMessage());
        }
    }
}
