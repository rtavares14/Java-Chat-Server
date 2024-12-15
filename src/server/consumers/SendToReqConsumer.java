package server.consumers;

import server.clientHelper.ClientInstance;
import server.loggers.ClientLogger;
import shared.messages.private_message.SendTo;
import shared.messages.private_message.SendToReq;
import shared.messages.private_message.SendToResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.CmdColors.WHITE;
import static shared.enumerations.ServerCommands.SENDTO;
import static shared.enumerations.ServerCommands.SENDTO_RESP;

public class SendToReqConsumer implements Consumer<String> {
    private final ClientInstance clientInstance;

    public SendToReqConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void accept(String jsonPayload) {
        try {
            SendToReq sendToReq = JsonUtils.fromJson(jsonPayload, SendToReq.class);
            String receiver = sendToReq.getUsername();
            String content = sendToReq.getMessage();

            if (clientInstance.getUsername() == "" || clientInstance.getUsername().isEmpty()) {
                SendToResp response = new SendToResp("ERROR", 6000);
                clientInstance.sendCommand(SENDTO_RESP, JsonUtils.toJson(response));
                MessageHelper.printColoredMessage(RED, "S --> (): " + SENDTO_RESP + " " + JsonUtils.toJson(response));
                return;
            }


            ClientInstance receiverInstance = ClientLogger.getInstance().getClient(receiver);
            if (receiverInstance == null) {
                SendToResp response = new SendToResp("ERROR", 6006);
                clientInstance.sendCommand(SENDTO_RESP, JsonUtils.toJson(response));
                MessageHelper.printColoredMessage(RED, "S --> (" + clientInstance.getUsername() + "): " + SENDTO_RESP + " " + JsonUtils.toJson(response));
            } else {
                // Send confirmation to the sender
                SendToResp response = new SendToResp("OK", null);
                clientInstance.sendCommand(SENDTO_RESP, JsonUtils.toJson(response));
                MessageHelper.printColoredMessage(WHITE, "S --> (" + clientInstance.getUsername() + "): " + SENDTO_RESP + " " + JsonUtils.toJson(response));

                // Send the message to the receiver
                SendTo sendTo = new SendTo(clientInstance.getUsername(), content);
                String json = JsonUtils.toJson(sendTo);

                receiverInstance.sendCommand(SENDTO, json);
                MessageHelper.printColoredMessage(WHITE, "C (" + clientInstance.getUsername() + ") --> C (" + receiver + "): " + SENDTO + " : " + jsonPayload);
            }
        } catch (Exception e) {
            System.err.println("Failed to process SENDTO_REQ message: " + e.getMessage());
        }
    }
}
