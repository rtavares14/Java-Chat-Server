package shared.messages.file_transfer;

public record FileTransfer(String sender,String filepath, Double size, String checkSum) {
}
