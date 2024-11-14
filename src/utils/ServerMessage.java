package utils;

import com.fasterxml.jackson.databind.JsonNode;

public class ServerMessage {

    private final String type;
    private final JsonNode data;

    /**
     * Constructs a new ServerMessage object.
     * @param type the type of the message.
     * @param data the data of the message.
     */
    public ServerMessage(String type, JsonNode data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Returns the type of the message.
     * @return the type of the message.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the data of the message.
     * @return the data of the message.
     */
    public JsonNode getData() {
        return data;
    }
}