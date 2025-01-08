package server.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.clientInstance.ClientInstance;
import shared.messages.file_transfer.choises.FileTransferChoiceResp;
import shared.utils.JsonUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static shared.enumerations.ServerCommands.FILET_CHOICE_RESP;

public class FileTransferHandler {

    private final Object transferLock = new Object(); //monitor for this transfer
    private ClientInstance sender;
    private ClientInstance receiver;
    private String fileName;
    private Double fileSize;
    private String checksum;
    private String uuid;
    private AtomicBoolean isAccepted = new AtomicBoolean(false);

    public FileTransferHandler(ClientInstance sender, ClientInstance receiver, String fileName, Double fileSize, String checksum, String uuid) {
        this.sender = sender;
        this.receiver = receiver;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.uuid = uuid;
    }

    public void startTransfer() {
        Thread transferThread = new Thread(() -> {
            synchronized (transferLock) {
                Timer timer = new Timer();
                try {
                    System.out.println("Waiting for receiver to accept the file transfer...");

                    // Start a 12-second timer for acceptance
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            synchronized (transferLock) {
                                if (!isAccepted.get()) {
                                    try {

                                        System.out.println("File transfer cancelled: Receiver did not accept within 12 seconds.");
                                        transferLock.notify();
                                        FileTransferChoiceResp response = new FileTransferChoiceResp("ERROR", 10009);
                                        sender.sendCommand(FILET_CHOICE_RESP, JsonUtils.toJson(response));
                                    } catch (JsonProcessingException e) {
                                        throw new RuntimeException(e);
                                    }

                                }
                            }
                        }
                    }, 12000);

                    // Wait acceptance decline or timeout
                    transferLock.wait();

                    if (isAccepted.get()) {
                        // YOU NEED TO IMPLEMENT THIS
                        // IT WILL DO IT BY ITSELF
                        // IT WILL DO IT BY ITSELF
                        // IT WILL DO IT BY ITSELF

                        System.out.println("File sent successfully to receiver " + receiver);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("File transfer interrupted: " + e.getMessage());
                } finally {
                    timer.cancel();
                }
            }
        });

        transferThread.start();
    }

    public void acceptTransfer() {
        synchronized (transferLock) {
            isAccepted.set(true);
            transferLock.notify();
        }
    }

    public void cancelTransfer() {
        synchronized (transferLock) {
            isAccepted.set(false);
            transferLock.notify();
        }
    }

    public ClientInstance getSender() {
        return sender;
    }

    public ClientInstance getReceiver() {
        return receiver;
    }

    public String getFileName() {
        return fileName;
    }

    public Double getFileSize() {
        return fileSize;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getUuid() {
        return uuid;
    }

    public AtomicBoolean getIsAccepted() {
        return isAccepted;
    }

    public Object getTransferLock() {
        return transferLock;
    }

    public void setStatusT() {
        isAccepted.set(true);
    }

    public void setStatusF() {
        isAccepted.set(false);
    }
}
