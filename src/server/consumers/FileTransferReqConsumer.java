package server.consumers;

import server.clientInstance.ClientInstance;
import server.handlers.FileTranfersHelpers.FileTransferTimer;
import server.handlers.FileTranfersHelpers.FileTransferRegistry;
import server.loggers.ClientLogger;
import shared.messages.file_transfer.request.FileTransfer;
import shared.messages.file_transfer.request.FileTransferReq;
import shared.messages.file_transfer.request.FileTransferResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.io.File;
import java.util.function.Consumer;

import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.CmdColors.TEAL;
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
            String receiver = fileTransferReq.receiver();
            String filepath = fileTransferReq.filepath();
            Double size = fileTransferReq.size();
            String checkSum = fileTransferReq.checkSum();

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
                        FileTransferResp response = new FileTransferResp("ERROR", 10001);
                        clientInstance.sendCommand(FILET_RESP, JsonUtils.toJson(response));
                        MessageHelper.printServerMessage(RED, clientInstance, FILET_RESP, JsonUtils.toJson(response));
                    } else {
                        // Send confirmation to the sender
                        FileTransferResp response = new FileTransferResp("OK", null);
                        clientInstance.sendCommand(FILET_RESP, JsonUtils.toJson(response));
                        MessageHelper.printServerMessage(TEAL, clientInstance, FILET_RESP, JsonUtils.toJson(response));

                        // Create a new file transfer session
                        String transferUuid = createUUID();
                        File transferFile = new File(filepath);
                        String fileName = transferFile.getName();
                        FileTransferTimer fileTransferTimer = new FileTransferTimer(clientInstance, receiverInstance, transferFile,checkSum, transferUuid);

                        // Send the message to the receiver
                        FileTransfer fileTransferReq1 = new FileTransfer(clientInstance.getUsername(), fileName, size, checkSum, transferUuid);
                        String json = JsonUtils.toJson(fileTransferReq1);

                        receiverInstance.sendCommand(FILET, json);
                        MessageHelper.printColoredMessage(TEAL, "S --> C (" + receiver + "): " + FILET + " : " + json);

                        fileTransferTimer.startTransfer();
                        FileTransferRegistry.getInstance().addSession(transferUuid, fileTransferTimer);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to process FILET_REQ message: " + e.getMessage());
        }
    }

    /**
     * Create a UUID
     * This method is used to create a UUID
     *
     * @return the UUID
     */
    private String createUUID() {
        // Create a random UUID
        // make it 7 characters long
        return java.util.UUID.randomUUID().toString().substring(0, 7);
    }
}
