package server.handlers.FileTranfersHelpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.clientInstance.ClientInstance;
import shared.enumerations.CmdColors;
import shared.messages.file_transfer.choises.FileTransferChoiceResp;
import shared.messages.file_transfer.status.FileTransferStart;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.FILET_CHOICE_RESP;
import static shared.enumerations.ServerCommands.FILET_START;

public class FileTransferTimer {

    private final AtomicBoolean isAccepted = new AtomicBoolean(false);
    private final ClientInstance sender;
    private final ClientInstance receiver;
    private final File file;
    private final String checksum;
    private final String uuid;

    public FileTransferTimer(ClientInstance sender, ClientInstance receiver, File file, String checksum, String uuid) {
        this.sender = sender;
        this.receiver = receiver;
        this.file = file;
        this.checksum = checksum;
        this.uuid = uuid;
    }

    public void startTransfer() {
        Timer timer = new Timer();
        try {
            MessageHelper.printColoredMessage(CmdColors.TEAL, "Waiting for receiver to accept the file transfer...");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isAccepted.get()) {
                        try {
                            MessageHelper.printColoredMessage(RED, "File transfer cancelled: Receiver did not accept within 30 seconds.");

                            FileTransferChoiceResp responseToReceiver = new FileTransferChoiceResp("ERROR", 10010);
                            receiver.sendCommand(FILET_CHOICE_RESP, JsonUtils.toJson(responseToReceiver));
                            MessageHelper.printServerMessage(RED, receiver, FILET_CHOICE_RESP, JsonUtils.toJson(responseToReceiver));

                            FileTransferChoiceResp responseToSender = new FileTransferChoiceResp("ERROR", 10009);
                            sender.sendCommand(FILET_CHOICE_RESP, JsonUtils.toJson(responseToSender));
                            MessageHelper.printServerMessage(RED, sender, FILET_CHOICE_RESP, JsonUtils.toJson(responseToSender));


                            cancelTransfer();
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }, 30000);

            if (isAccepted.get()) {
                MessageHelper.printColoredMessage(CmdColors.TEAL, "Receiver accepted the file transfer, starting transfer...");
                FileTransferStart fileTransferStart = new FileTransferStart(sender.getUsername(), receiver.getUsername(), file, uuid, checksum);
                String jsonPayload = JsonUtils.toJson(fileTransferStart);
                sender.sendCommand(FILET_START, jsonPayload);
                receiver.sendCommand(FILET_START, jsonPayload);

                // Start the file transfer real logic
                transferFile();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            timer.cancel();
        }
    }


    private void transferFile() {
        //connect the 2 clients to port 1338
        //send bytes to that port
        //S or R as a role
        //uuid as a session id
        //and the file

        // Call file transfer handler
        // loop thought the all sessions
    }

    public void acceptTransfer() {
        isAccepted.set(true);

    }

    public void cancelTransfer() {
        FileTransferRegistry.getInstance().removeSession(uuid);
        isAccepted.set(false);

        MessageHelper.printColoredMessage(RED, "File transfer cancelled, removing session " + uuid);
    }

    public ClientInstance getSender() {
        return sender;
    }

    public ClientInstance getReceiver() {
        return receiver;
    }

    public String getUuid() {
        return uuid;
    }
}
