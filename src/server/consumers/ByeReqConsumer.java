package server.consumers;

import server.clientInstance.ClientInstance;
import server.loggers.ClientLogger;
import server.loggers.ServerLogger;
import shared.messages.login_logout.ByeResp;
import shared.messages.login_logout.Left;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.PURPLE;
import static shared.enumerations.ServerCommands.BYE_RESP;
import static shared.enumerations.ServerCommands.LEFT;

public class ByeReqConsumer implements Consumer<String> {
    private final ClientInstance clientInstance;

    public ByeReqConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void accept(String jsonPayload) {
        try {
            ByeResp byeResp = new ByeResp("OK");
            clientInstance.sendCommand(BYE_RESP, JsonUtils.toJson(byeResp));

            Left left = new Left(clientInstance.getUsername());

            // Remove the client from the allClients list
            ClientLogger.getInstance().removeUser(clientInstance.getUsername());
            ClientLogger.getInstance().getAllClients().remove(this);

            clientInstance.cleanup();
            MessageHelper.printServerMessage(PURPLE, clientInstance, BYE_RESP, JsonUtils.toJson(byeResp));
            ServerLogger.getInstance().broadcastMessage(left, clientInstance.getUsername(), LEFT);
            ServerLogger.getInstance().getClientUserCounts();

        } catch (Exception e) {
            System.err.println("Failed to process BYE_REQ message: " + e.getMessage());
        }
    }
}