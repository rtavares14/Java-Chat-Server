package server.consumers.fileTransfer;

import server.clientInstance.ClientInstance;
import server.FileTranfersHelpers.FileTransferRegistry;
import server.FileTranfersHelpers.FileTransferTimer;
import shared.messages.file_transfer.choises.FileTransferACPReq;
import shared.messages.file_transfer.choises.FileTransferACPResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.CmdColors.TEAL;
import static shared.enumerations.ServerCommands.FILET_ACP_RESP;

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
            FileTransferTimer handler = registry.getSession(uuid);

            if (clientInstance.getUsername() == null || clientInstance.getUsername().isEmpty()) {
                FileTransferACPResp response = new FileTransferACPResp("ERROR", 6000);
                clientInstance.sendCommand(FILET_ACP_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, FILET_ACP_RESP, JsonUtils.toJson(response));
                return;
            }

            if (handler == null) {
                FileTransferACPResp response = new FileTransferACPResp("ERROR", 10006);
                clientInstance.sendCommand(FILET_ACP_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, FILET_ACP_RESP, JsonUtils.toJson(response));
                return;
            }

            if (!handler.getSender().getUsername().equals(sender)) {
                FileTransferACPResp response = new FileTransferACPResp("ERROR", 10007);
                clientInstance.sendCommand(FILET_ACP_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, FILET_ACP_RESP, JsonUtils.toJson(response));
                return;
            }

            FileTransferACPResp response = new FileTransferACPResp("OK", null);
            clientInstance.sendCommand(FILET_ACP_RESP, JsonUtils.toJson(response));
            MessageHelper.printServerMessage(TEAL, clientInstance, FILET_ACP_RESP, JsonUtils.toJson(response));
            handler.acceptTransfer();
        } catch (Exception e) {
            System.err.println("Failed to process FILET_CHOICE_REQ message: " + e.getMessage());
        }
    }
}
