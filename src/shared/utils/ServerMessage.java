package shared.utils;

import com.fasterxml.jackson.databind.JsonNode;
import shared.enumerations.ServerCommands;

public class ServerMessage {

    private final ServerCommands type;
    private final JsonNode data;

    /**
     * Constructs a new ServerMessage object.
     *
     * @param type the type of the message.
     * @param data the data of the message.
     */
    public ServerMessage(ServerCommands type, JsonNode data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Returns the type of the message.
     *
     * @return the type of the message.
     */
    public ServerCommands getType() {
        return type;
    }

    /**
     * Returns the data of the message.
     *
     * @return the data of the message.
     */
    public JsonNode getData() {
        return data;
    }
}