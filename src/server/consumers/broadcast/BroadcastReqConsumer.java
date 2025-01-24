package server.consumers.broadcast;

import server.clientInstance.ClientInstance;
import server.loggers.ClientLogger;
import server.loggers.ServerLogger;
import shared.messages.broadcast.Broadcast;
import shared.messages.broadcast.BroadcastReq;
import shared.messages.broadcast.BroadcastResp;
import shared.messages.list.List;
import shared.messages.list.ListResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;
import static shared.enumerations.ServerCommands.*;

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

    public static class ListReqConsumer implements Consumer<String> {
        private final ClientInstance clientInstance;

        public ListReqConsumer(ClientInstance clientInstance) {
            this.clientInstance = clientInstance;
        }

        @Override
        public void accept(String jsonPayload) {
            try {
                if (clientInstance.getUsername() == "" || clientInstance.getUsername().isEmpty()) {
                    ListResp response = new ListResp("ERROR", 6000);
                    clientInstance.sendCommand(LIST_RESP, JsonUtils.toJson(response));
                    MessageHelper.printServerMessage(RED, clientInstance, LIST_RESP, JsonUtils.toJson(response));
                    return;
                }

                ListResp response = new ListResp("OK", null);
                clientInstance.sendCommand(LIST_RESP, JsonUtils.toJson(response));
                MessageHelper.printServerMessage(CYAN, clientInstance, LIST_RESP, JsonUtils.toJson(response));


                List list = new List(ClientLogger.getInstance().getClients());
                clientInstance.sendCommand(LIST, JsonUtils.toJson(list));
                MessageHelper.printServerMessage(CYAN, clientInstance, LIST, JsonUtils.toJson(response));

            } catch (Exception e) {
                System.err.println("Failed to process LIST message: " + e.getMessage());
            }
        }
    }
}