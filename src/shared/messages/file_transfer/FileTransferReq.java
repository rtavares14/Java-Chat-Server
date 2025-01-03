package shared.messages.file_transfer;

public record FileTransferReq(String receiver, String filepath, Double size, String checkSum) {

    public String getReceiver() {
        return receiver;
    }

    public String getFilepath() {
        return filepath;
    }

    public Double getSize() {
        return size;
    }

    public String getCheckSum() {
        return checkSum;
    }
}
