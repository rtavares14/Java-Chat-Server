package client.consumers.fileTransfer;

import client.fileTransfer.TransferClientSender;
import client.fileTransfer.TransferClientReceiver;
import shared.messages.file_transfer.status.FileTransferStart;
import shared.utils.JsonUtils;

import java.io.File;
import java.util.function.Consumer;

public class FileTStartConsumer implements Consumer<String> {

    @Override
    public void accept(String jsonPayload) {
        try {
            FileTransferStart resp = JsonUtils.fromJson(jsonPayload, FileTransferStart.class);
            File file = resp.file();
            String uuid = resp.uuid();

            char role = uuid.toUpperCase().charAt(uuid.length()-1);

            //sender if not receiver
            if (role == 'S') {
                new Thread(new TransferClientSender(removeRole(uuid),file.getAbsoluteFile())).start();
            } else if (role == 'R') {
                new Thread(new TransferClientReceiver(removeRole(uuid), file.getName())).start();
            }else {
                System.err.println("Something is now right");
            }

        } catch (Exception e) {
            System.err.println("Failed to process FILET_START message: " + e.getMessage());
        }
    }

    /**
     * Removes the last character from the uuid
     * @param uuid the uuid to remove the last character from
     * @return the uuid without the last character
     */
    public String removeRole(String uuid) {
        return uuid.substring(0, uuid.length() - 1);
    }
}
