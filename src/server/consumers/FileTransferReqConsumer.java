package server.consumers;

import server.clientHelper.ClientInstance;
import server.loggers.ClientLogger;
import shared.messages.file_transfer.FileTransfer;
import shared.messages.file_transfer.FileTransferReq;
import shared.messages.file_transfer.FileTransferResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;
import static shared.enumerations.ServerCommands.FILET;
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
            Double size = fileTransferReq.getSize();
            String checkSum = fileTransferReq.getCheckSum();

            if (clientInstance.getUsername() == null || clientInstance.getUsername().isEmpty()) {
                FileTransferResp response = new FileTransferResp("ERROR", 6000);
                clientInstance.sendCommand(FILET_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, FILET_RESP, JsonUtils.toJson(response));
                return;
            }

            ClientInstance receiverInstance = ClientLogger.getInstance().getClient(receiver);
            if (receiverInstance == clientInstance) {
                FileTransferResp response = new FileTransferResp("ERROR", 10000);
                clientInstance.sendCommand(FILET_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, FILET_RESP, JsonUtils.toJson(response));
            } else {
                if (receiverInstance == null) {
                    FileTransferResp response = new FileTransferResp("ERROR", 6006);
                    clientInstance.sendCommand(FILET_RESP, JsonUtils.toJson(response));
                    MessageHelper.printServerMessage(RED, clientInstance, FILET_RESP, JsonUtils.toJson(response));
                } else {
                    if (filepath.isEmpty() || size == 0 || checkSum.isEmpty()) {
                        FileTransferResp response = new FileTransferResp("ERROR", 10000);
                        clientInstance.sendCommand(FILET_RESP, JsonUtils.toJson(response));
                        MessageHelper.printServerMessage(RED, clientInstance, FILET_RESP, JsonUtils.toJson(response));
                    } else {
                        // Send confirmation to the sender
                        FileTransferResp response = new FileTransferResp("OK", null);
                        clientInstance.sendCommand(FILET_RESP, JsonUtils.toJson(response));
                        MessageHelper.printServerMessage(TEAL, clientInstance, FILET_RESP, JsonUtils.toJson(response));

                        // Send the message to the receiver
                        FileTransfer fileTransferReq1 = new FileTransfer(clientInstance.getUsername(), filepath, size, checkSum);
                        String json = JsonUtils.toJson(fileTransferReq1);

                        receiverInstance.sendCommand(FILET, json);
                        MessageHelper.printColoredMessage(TEAL, "C (" + clientInstance.getUsername() + ") --> C (" + receiver + "): " + FILET + " : " + json);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to process FILIET_REQ message: " + e.getMessage());
        }
    }
}
