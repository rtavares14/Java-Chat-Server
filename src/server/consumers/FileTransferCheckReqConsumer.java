package server.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.clientInstance.ClientInstance;
import server.handlers.FileTranfersHelpers.FileTransferRegistry;
import shared.messages.file_transfer.request.FileTransferResp;
import shared.messages.file_transfer.status.FileTransferCheckReq;
import shared.messages.file_transfer.status.FileTransferEnd;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.CmdColors.TEAL;
import static shared.enumerations.ServerCommands.FILET_END;
import static shared.enumerations.ServerCommands.FILET_RESP;

public class FileTransferCheckReqConsumer implements Consumer<String> {
    public final ClientInstance clientInstance;

    public FileTransferCheckReqConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void accept(String JsonPayload) {
        try {
            FileTransferCheckReq fileTransferCheckReq = JsonUtils.fromJson(JsonPayload, FileTransferCheckReq.class);

            if (fileTransferCheckReq.Cstatus().equals("GOOD")) {
                MessageHelper.printColoredMessage(TEAL, "File transfer successful, checksums match");
                FileTransferRegistry.addToTT();

                FileTransferEnd fileTransferEnd = new FileTransferEnd("OK", null);
                ClientInstance sender = FileTransferRegistry.getInstance().getSession(fileTransferCheckReq.uuid()).getSender();
                ClientInstance receiver = FileTransferRegistry.getInstance().getSession(fileTransferCheckReq.uuid()).getReceiver();

                sender.sendCommand(FILET_END, JsonUtils.toJson(fileTransferEnd));
                receiver.sendCommand(FILET_END, JsonUtils.toJson(fileTransferEnd));

                MessageHelper.printServerMessage(TEAL, sender, FILET_RESP, JsonUtils.toJson(fileTransferEnd));
                MessageHelper.printServerMessage(TEAL, receiver, FILET_RESP, JsonUtils.toJson(fileTransferEnd));

                FileTransferRegistry.getInstance().removeSession(fileTransferCheckReq.uuid());
                FileTransferRegistry.getInstance().removeHandler(fileTransferCheckReq.uuid());

                FileTransferRegistry.getInstance().printSessions();
            } else {
                MessageHelper.printColoredMessage(RED, "File transfer failed, checksums do not match");
                FileTransferResp fileTransferResp = new FileTransferResp("ERROR", 10005);
                ClientInstance sender = FileTransferRegistry.getInstance().getSession(fileTransferCheckReq.uuid()).getSender();
                ClientInstance receiver = FileTransferRegistry.getInstance().getSession(fileTransferCheckReq.uuid()).getReceiver();

                sender.sendCommand(FILET_RESP, JsonUtils.toJson(fileTransferResp));
                receiver.sendCommand(FILET_RESP, JsonUtils.toJson(fileTransferResp));

                FileTransferRegistry.getInstance().removeSession(fileTransferCheckReq.uuid());
                FileTransferRegistry.getInstance().removeHandler(fileTransferCheckReq.uuid());
                MessageHelper.printServerMessage(RED, sender, FILET_RESP, JsonUtils.toJson(fileTransferResp));
                MessageHelper.printServerMessage(RED, receiver, FILET_RESP, JsonUtils.toJson(fileTransferResp));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
