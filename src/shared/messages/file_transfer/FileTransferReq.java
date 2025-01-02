package shared.messages.file_transfer;

public record FileTransferReq(String receiver, String filepath, Integer size, String checkSum) {

    public String getReceiver() {
        return receiver;
    }

    public String getFilepath() {
        return filepath;
    }

    public Integer getSize() {
        return size;
    }

    public String getCheckSum() {
        return checkSum;
    }
}
