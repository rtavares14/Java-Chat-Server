package server.consumers;

import server.clientInstance.ClientInstance;
import server.handlers.FileTransferHandler;
import server.handlers.FileTransferRegistry;
import shared.messages.file_transfer.choises.FileTransferACPReq;
import shared.messages.file_transfer.choises.FileTransferChoiceResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;
import static shared.enumerations.ServerCommands.*;

public class FileTransferACPReqConsumer implements Consumer<String> {
    private final ClientInstance clientInstance;


    public FileTransferACPReqConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void accept(String jsonPayload) {
        try {
            FileTransferACPReq fileTransferChoiceReq = JsonUtils.fromJson(jsonPayload, FileTransferACPReq.class);
            String sender = fileTransferChoiceReq.sender();
            String uuid = fileTransferChoiceReq.uuid();

            FileTransferRegistry registry = FileTransferRegistry.getInstance();
            FileTransferHandler handler = registry.getSession(uuid);

            if (clientInstance.getUsername() == null || clientInstance.getUsername().isEmpty()) {
                FileTransferChoiceResp response = new FileTransferChoiceResp("ERROR", 6000);
                clientInstance.sendCommand(FILET_CHOICE_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, FILET_CHOICE_RESP, JsonUtils.toJson(response));
            }

            if (handler == null) {
                FileTransferChoiceResp response = new FileTransferChoiceResp("ERROR", 10006);
                clientInstance.sendCommand(FILET_CHOICE_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, FILET_CHOICE_RESP, JsonUtils.toJson(response));
            } else {
                if (!handler.getSender().getUsername().equals(sender)) {
                    FileTransferChoiceResp response = new FileTransferChoiceResp("ERROR", 10007);
                    clientInstance.sendCommand(FILET_CHOICE_RESP, JsonUtils.toJson(response));
                    MessageHelper.printServerMessage(RED, clientInstance, FILET_CHOICE_RESP, JsonUtils.toJson(response));
                } else {
                    handler.acceptTransfer();
                    System.out.println("File transfer started");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to process FILET_CHOICE_REQ message: " + e.getMessage());
        }
    }



}
