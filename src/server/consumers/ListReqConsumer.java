package server.consumers;

import server.clientHelper.ClientInstance;
import server.loggers.ClientLogger;
import shared.messages.list.List;
import shared.messages.list.ListResp;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.CYAN;
import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.LIST;
import static shared.enumerations.ServerCommands.LIST_RESP;

public class ListReqConsumer implements Consumer<String> {
    private final ClientInstance clientInstance;

    public ListReqConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void accept(String string) {
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
