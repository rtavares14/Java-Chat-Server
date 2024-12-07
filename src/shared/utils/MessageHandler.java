package shared.utils;

import client.consummers.*;
import shared.enumerations.ServerCommands;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static shared.enumerations.ServerCommands.*;

public class MessageHandler {
    private final Map<ServerCommands, Consumer<String>> handlersClient = new HashMap<>();
    private final Map<ServerCommands, Consumer<String>> handlersServer = new HashMap<>();
    private final PrintWriter writer;

    /**
     * Constructor
     * MessageHandler constructor that initializes the writer and handlersClient map with the appropriate consumers
     *
     * @param writer writer
     */
    public MessageHandler(PrintWriter writer) {
        this.writer = writer;
        handlersClient.put(PING, new PingConsumer(writer));
        handlersClient.put(READY, new ReadyConsumer());
        handlersClient.put(ENTER_RESP, new EnterRespConsumer());
        handlersClient.put(BROADCAST_RESP, new BroadcastRespConsumer());
        handlersClient.put(BROADCAST, new BroadcastConsumer());
        handlersClient.put(LEFT, new LeftConsumer());
        handlersClient.put(BYE_RESP, new ByeConsumer());
        handlersClient.put(HANGUP, new HangupConsumer());
        handlersClient.put(JOINED, new JoinedConsumer());
        handlersClient.put(SENDTO_RESP, new SendToRespConsumer());
        handlersClient.put(SENDTO, new SendToConsumer());
        handlersClient.put(LIST, new ListConsumer());
        handlersClient.put(LIST_RESP, new ListRespConsumer());


    }

    /**
     * Handle message
     * This method is used to handle messages from the server
     *
     * @param command command
     * @param json json
     */
    public void handleMessage(ServerCommands command, String json) {
        Consumer<String> handler = handlersClient.get(command);
        if (handler != null) {
            handler.accept(json);
        } else {
            System.err.println("Unknown command: " + command);
        }
    }
}