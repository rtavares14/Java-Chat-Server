package server.FileTranfersHelpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.clientInstance.ClientInstance;
import shared.enumerations.CmdColors;
import shared.messages.file_transfer.choises.FileTransferACPResp;
import shared.messages.file_transfer.status.FileTransferStart;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.FILET_REJ_RESP;
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

    /**
     * Start the timer for the file transfer
     * This method is used to start the timer for the file session
     * It waits for the receiver to accept the file transfer
     * If the receiver does not accept within 30 seconds, the method cancels the transfer
     * And sends a rejection response to both the sender and the receiver
     */

    public void startTimer() {
        Timer timer = new Timer();
        try {
            MessageHelper.printColoredMessage(CmdColors.TEAL, "Waiting for receiver to accept the file transfer...");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isAccepted.compareAndSet(false, true)) {
                        try {
                            MessageHelper.printColoredMessage(RED, "File transfer cancelled: Receiver did not accept within 15 seconds.");

                            FileTransferACPResp responseToReceiver = new FileTransferACPResp("ERROR", 10010);
                            receiver.sendCommand(FILET_REJ_RESP, JsonUtils.toJson(responseToReceiver));
                            MessageHelper.printServerMessage(CmdColors.RED, receiver, FILET_REJ_RESP, JsonUtils.toJson(responseToReceiver));

                            FileTransferACPResp responseToSender = new FileTransferACPResp("ERROR", 10009);
                            sender.sendCommand(FILET_REJ_RESP, JsonUtils.toJson(responseToSender));
                            MessageHelper.printServerMessage(CmdColors.RED, sender, FILET_REJ_RESP, JsonUtils.toJson(responseToSender));

                            cancelTransfer();
                            timer.cancel();
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        } finally {
                            timer.cancel();
                        }
                    }
                }
            }, 15000); //15 seconds timeout

        } catch (Exception e) {
            MessageHelper.printColoredMessage(CmdColors.RED, "An error occurred while starting the timer: " + e.getMessage());
        }
    }

    /**
     * Accept the file transfer
     * This method is used to accept the file transfer
     * It sends a start message to both the sender and the receiver
     * If an error occurs, the method prints the stack trace
     */
    public void acceptTransfer() throws JsonProcessingException {
        isAccepted.set(true);
        MessageHelper.printColoredMessage(CmdColors.TEAL, "Receiver accepted the file transfer, starting transfer...");
        FileTransferStart fileTransferStartS = new FileTransferStart(sender.getUsername(), receiver.getUsername(), file, uuid + "S", checksum);
        String jsonPayloadSender = JsonUtils.toJson(fileTransferStartS);
        FileTransferStart fileTransferStartR = new FileTransferStart(sender.getUsername(), receiver.getUsername(), file, uuid + "R", checksum);
        String jsonPayloadReceiver = JsonUtils.toJson(fileTransferStartR);
        sender.sendCommand(FILET_START, jsonPayloadSender);
        receiver.sendCommand(FILET_START, jsonPayloadReceiver);
        MessageHelper.printServerMessage(CmdColors.TEAL, sender, FILET_START, jsonPayloadSender);
        MessageHelper.printServerMessage(CmdColors.TEAL, receiver, FILET_START, jsonPayloadReceiver);
    }

    /**
     * Cancel the file transfer
     * This method is used to cancel the file transfer
     * It removes the session from the registry
     * And prints a message to the console
     */
    public void cancelTransfer() {
        FileTransferRegistry.getInstance().removeSession(uuid);
        isAccepted.set(true);

        MessageHelper.printColoredMessage(RED, "File transfer cancelled, removing session " + uuid);
    }

    /**
     * Get the sender
     *
     * @return the sender
     */
    public ClientInstance getSender() {
        return sender;
    }

    /**
     * Get the receiver
     *
     * @return the receiver
     */
    public ClientInstance getReceiver() {
        return receiver;
    }

    /**
     * Get the uuid
     *
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }
}
