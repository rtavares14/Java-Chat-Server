package server.consumers;

import server.clientHelper.ClientInstance;
import server.loggers.ClientLogger;
import shared.messages.file_transfer.FileTransferReq;
import shared.messages.file_transfer.FileTransferResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.FILET_RESP;

public class FileTransferReqConsumer implements Consumer<String> {
    private final ClientInstance clientInstance;


    public FileTransferReqConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void accept(String jsonPayload) {
        try {
            FileTransferReq fileTransferReq = JsonUtils.fromJson(jsonPayload, FileTransferReq.class);
            String receiver = fileTransferReq.getReceiver();
            String filepath = fileTransferReq.getFilepath();
            Integer size = fileTransferReq.getSize();
            String checkSum = fileTransferReq.getCheckSum();

            if (clientInstance.getUsername() == null || clientInstance.getUsername().isEmpty()) {
                FileTransferResp response = new FileTransferResp("ERROR", 6000);
                clientInstance.sendCommand(FILET_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, FILET_RESP, JsonUtils.toJson(response));
                return;
            }

            ClientInstance receiverInstance = ClientLogger.getInstance().getClient(receiver);
            if (receiverInstance == null) {
                FileTransferResp response = new FileTransferResp("ERROR", 6006);
                clientInstance.sendCommand(FILET_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, FILET_RESP, JsonUtils.toJson(response));
            }else {
                if (filepath.isEmpty() || size == 0 || checkSum.isEmpty()) {
                    FileTransferResp response = new FileTransferResp("ERROR", 6007);
                    clientInstance.sendCommand(FILET_RESP, JsonUtils.toJson(response));
                    MessageHelper.printServerMessage(RED, clientInstance, FILET_RESP, JsonUtils.toJson(response));
                } else {
                    //need to checj file path if it exists
                    // Send confirmation to the sender

                }
            }


        } catch (Exception e) {
            System.err.println("Failed to process FILIET_REQ message: " + e.getMessage());
        }
    }
}
