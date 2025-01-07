package server.consumers;

import server.clientHelper.ClientInstance;
import server.loggers.ServerLogger;
import shared.messages.broadcast.Broadcast;
import shared.messages.broadcast.BroadcastReq;
import shared.messages.broadcast.BroadcastResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.ORANGE;
import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.BROADCAST;
import static shared.enumerations.ServerCommands.BROADCAST_RESP;

public class BroadcastReqConsumer implements Consumer<String> {
    private final ClientInstance clientInstance;

    public BroadcastReqConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void accept(String jsonPayload) {
        try {
            BroadcastReq broadcastReq = JsonUtils.fromJson(jsonPayload, BroadcastReq.class);
            if (clientInstance.getUsername() == null || clientInstance.getUsername().isEmpty()) {
                BroadcastResp response = new BroadcastResp("ERROR", 6000);
                clientInstance.sendCommand(BROADCAST_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(RED, clientInstance, BROADCAST_RESP, JsonUtils.toJson(response));
                return;
            }

            // Send confirmation to the sender
            BroadcastResp response = new BroadcastResp("OK", null);
            clientInstance.sendCommand(BROADCAST_RESP, JsonUtils.toJson(response));
            MessageHelper.printServerMessage(ORANGE, clientInstance, BROADCAST_RESP, JsonUtils.toJson(response));

            // Broadcast the message to all other clients
            Broadcast broadcast = new Broadcast(clientInstance.getUsername(), broadcastReq.getMessage());
            ServerLogger.getInstance().broadcastMessage(broadcast, clientInstance.getUsername(), BROADCAST);
        } catch (Exception e) {
            System.err.println("Failed to process BROADCAST_REQ message: " + e.getMessage());
        }
    }
}