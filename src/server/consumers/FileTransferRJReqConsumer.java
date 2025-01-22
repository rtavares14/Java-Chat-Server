package server.consumers;

import server.clientInstance.ClientInstance;
import server.handlers.FileTranfersHelpers.FileTransferRegistry;
import server.handlers.FileTranfersHelpers.FileTransferTimer;
import shared.messages.file_transfer.choises.FileTransferACPResp;
import shared.messages.file_transfer.choises.FileTransferRJReq;
import shared.messages.file_transfer.status.FileTransferRejected;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.CmdColors.TEAL;
import static shared.enumerations.ServerCommands.FILET_REJ_RESP;
import static shared.enumerations.ServerCommands.FILET_REJ;

public class FileTransferRJReqConsumer implements Consumer<String> {
    private final ClientInstance clientInstance;


    public FileTransferRJReqConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void accept(String jsonPayload) {
        try {
            FileTransferRJReq fileTransferChoiceReq = JsonUtils.fromJson(jsonPayload, FileTransferRJReq.class);
            String sender = fileTransferChoiceReq.sender();
            String uuid = fileTransferChoiceReq.uuid();

            FileTransferRegistry registry = FileTransferRegistry.getInstance();
            FileTransferTimer handler = registry.getSession(uuid);

            if (clientInstance.getUsername() == null || clientInstance.getUsername().isEmpty()) {
                FileTransferACPResp response = new FileTransferACPResp("ERROR", 6000);
                clientInstance.sendCommand(FILET_REJ_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, FILET_REJ_RESP, JsonUtils.toJson(response));
                return;
            }

            if (handler == null) {
                FileTransferACPResp response = new FileTransferACPResp("ERROR", 10006);
                clientInstance.sendCommand(FILET_REJ_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, FILET_REJ_RESP, JsonUtils.toJson(response));
                return;
            }

            if (!handler.getSender().getUsername().equals(sender)) {
                FileTransferACPResp response = new FileTransferACPResp("ERROR", 10007);
                clientInstance.sendCommand(FILET_REJ_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, FILET_REJ_RESP, JsonUtils.toJson(response));
                return;
            }


            FileTransferACPResp response = new FileTransferACPResp("OK", null);
            clientInstance.sendCommand(FILET_REJ_RESP, JsonUtils.toJson(response));
            MessageHelper.printServerMessage(TEAL, clientInstance, FILET_REJ_RESP, JsonUtils.toJson(response));

            FileTransferRejected fileTransferRejected = new FileTransferRejected("OK", null);
            handler.getSender().sendCommand(FILET_REJ, JsonUtils.toJson(fileTransferRejected));
            MessageHelper.printServerMessage(TEAL, handler.getSender(), FILET_REJ, JsonUtils.toJson(fileTransferRejected));
            handler.cancelTransfer();
        } catch (Exception e) {
            System.err.println("Failed to process FILET_CHOICE_REQ message: " + e.getMessage());
        }
    }
}
