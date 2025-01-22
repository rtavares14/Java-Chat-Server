package server.handlers.FileTranfersHelpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.clientInstance.ClientInstance;
import shared.enumerations.CmdColors;
import shared.messages.file_transfer.choises.FileTransferACPResp;
import shared.messages.file_transfer.status.FileTransferStart;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.io.File;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.*;

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

                            FileTransferACPResp responseToReceiver = new FileTransferACPResp("ERROR", 10010);
                            receiver.sendCommand(FILET_REJ_RESP, JsonUtils.toJson(responseToReceiver));
                            MessageHelper.printServerMessage(RED, receiver, FILET_REJ_RESP, JsonUtils.toJson(responseToReceiver));

                            FileTransferACPResp responseToSender = new FileTransferACPResp("ERROR", 10009);
                            sender.sendCommand(FILET_REJ_RESP, JsonUtils.toJson(responseToSender));
                            MessageHelper.printServerMessage(RED, sender, FILET_REJ_RESP, JsonUtils.toJson(responseToSender));


                            cancelTransfer();
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }, 30000);

        } finally {
            timer.cancel();
        }
    }


    private void transferToLogic() {
        //connect the 2 clients to port 1338
        //send bytes to that port
        //S or R as a role
        //uuid as a session id
        //and the file

        // Call file transfer handler
        // loop thought the all sessions

        try {
            // BIMBAMBUM connect the 2 clients to port 1338
            Socket senderSocket = new Socket(sender.getSocket().getInetAddress(), 1338);
            Socket receiverSocket = new Socket(receiver.getSocket().getInetAddress(), 1338);

            // BIMBAMBUM send bytes to that port
            senderSocket.getOutputStream().write(("S," + uuid).getBytes());
            receiverSocket.getOutputStream().write(("R," + uuid).getBytes());


        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.printColoredMessage(RED, "Failed to establish file transfer connection for session " + uuid);
        }
    }

    public void acceptTransfer() throws JsonProcessingException {
        isAccepted.set(true);
        MessageHelper.printColoredMessage(CmdColors.TEAL, "Receiver accepted the file transfer, starting transfer...");
        FileTransferStart fileTransferStartS = new FileTransferStart(sender.getUsername(), receiver.getUsername(), file, uuid+"S", checksum);
        String jsonPayloadSender = JsonUtils.toJson(fileTransferStartS);
        FileTransferStart fileTransferStartR = new FileTransferStart(sender.getUsername(), receiver.getUsername(), file, uuid+"R", checksum);
        String jsonPayloadReceiver = JsonUtils.toJson(fileTransferStartR);
        sender.sendCommand(FILET_START, jsonPayloadSender);
        receiver.sendCommand(FILET_START, jsonPayloadReceiver);
        MessageHelper.printServerMessage(CmdColors.TEAL, sender, FILET_START, jsonPayloadSender);
        MessageHelper.printServerMessage(CmdColors.TEAL, receiver, FILET_START, jsonPayloadReceiver);
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
