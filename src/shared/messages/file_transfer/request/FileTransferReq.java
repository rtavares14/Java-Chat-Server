package shared.messages.file_transfer.request;

public record FileTransferReq(String receiver, String filepath, Double size, String checkSum) {
}
