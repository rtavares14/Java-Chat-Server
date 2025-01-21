package shared.messages.file_transfer.status;

import java.io.File;

public record FileTransferStart(String sender, String receiver, File file, String uuid, String checkSum) {
}
