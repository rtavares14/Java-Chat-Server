package shared.utils.messages;

import shared.consummers.*;
import shared.enumerations.ServerCommands;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static shared.enumerations.ServerCommands.*;

public class MessageHandler {
    private final Map<ServerCommands, Consumer<String>> handlers = new HashMap<>();
    private final PrintWriter writer;

    /**
     * Constructor
     * MessageHandler constructor that initializes the writer and handlers map with the appropriate consumers
     *
     * @param writer writer
     */
    public MessageHandler(PrintWriter writer) {
        this.writer = writer;
        handlers.put(PING, new PingConsumer(writer));
        handlers.put(READY, new ReadyConsumer());
        handlers.put(ENTER_RESP, new EnterRespConsumer());
        handlers.put(BROADCAST_RESP, new BroadcastRespConsumer());
        handlers.put(BROADCAST, new BroadcastConsumer());
        handlers.put(LEFT, new LeftConsumer());
        handlers.put(BYE_RESP, new ByeConsumer());
        handlers.put(HANGUP, new HangupConsumer());
        handlers.put(JOINED, new JoinedConsumer());
        handlers.put(SENDTO_RESP, new SendToRespConsumer());
        handlers.put(SENDTO, new SendToConsumer());
        handlers.put(LIST, new ListConsumer());
        handlers.put(LIST_RESP, new ListRespConsumer());
        handlers.put(UNKNOWN_COMMAND, new UnknownConsumer());
        handlers.put(PARSE_ERROR, new ParseConsumer());
    }

    /**
     * Handle message
     * This method is used to handle messages from the server
     *
     * @param command command
     * @param json json
     */
    public void handleMessage(ServerCommands command, String json) {
        Consumer<String> handler = handlers.get(command);
        if (handler != null) {
            handler.accept(json);
        } else {
            System.err.println("Unknown command: " + command);
        }
    }
}