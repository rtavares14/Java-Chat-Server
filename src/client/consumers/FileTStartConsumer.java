package client.consumers;

import server.clientInstance.ClientInstance;
import shared.messages.file_transfer.request.FileTransferResp;
import shared.messages.file_transfer.status.FileTransferStart;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.io.File;
import java.util.function.Consumer;

import static shared.enumerations.CmdColors.TEAL;

public class FileTStartConsumer implements Consumer<String> {

    @Override
    public void accept(String jsonPayload) {
        try {
            FileTransferStart resp = JsonUtils.fromJson(jsonPayload, FileTransferStart.class);
            String uuid = resp.uuid();

            System.out.println(uuid);


        } catch (Exception e) {
            System.err.println("Failed to process FILET_START message: " + e.getMessage());
        }
    }
}
