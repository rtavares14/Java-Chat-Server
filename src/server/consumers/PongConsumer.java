package server.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.clientHelper.ClientInstance;
import shared.messages.ping_pong.PongError;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.PURPLE;
import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.PONG_ERROR;

public class PongConsumer implements Consumer<String> {
    private final ClientInstance clientInstance;

    public PongConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void accept(String json) {
        try {

            synchronized (this) {
                if (clientInstance.isExpectingPong()) {
                    clientInstance.setExpectingPong(false);
                    MessageHelper.printColoredMessage(PURPLE, clientInstance.getUsername() + " heartbeat is still alive!");
                } else {
                    PongError pongError = new PongError(8000);
                    json = JsonUtils.toJson(pongError);
                    clientInstance.sendCommand(PONG_ERROR, json);
                    MessageHelper.printServerMessage(RED, clientInstance, PONG_ERROR, PONG_ERROR.toString());
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
