package shared.utils.messages;

import client.consumers.*;
import server.clientHelper.ClientInstance;
import server.consumers.*;
import shared.enumerations.ServerCommands;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.*;

public class MessageHandler {
    private final Map<ServerCommands, Consumer<String>> handlersC = new HashMap<>();
    private final Map<ServerCommands, Consumer<String>> handlersS = new HashMap<>();
    private final PrintWriter writer;
    private final ClientInstance clientInstance;

    /**
     * Constructor
     * MessageHandler constructor that initializes the writer
     * handlers map with the appropriate consumers
     *
     * @param writer writer
     */
    public MessageHandler(PrintWriter writer,ClientInstance clientInstance) {
        this.writer = writer;
        this.clientInstance = clientInstance;

        // User consumers commands that the server can send
        handlersC.put(PING, new PingConsumer(writer));
        handlersC.put(READY, new ReadyConsumer());
        handlersC.put(ENTER_RESP, new EnterRespConsumer());
        handlersC.put(BROADCAST_RESP, new BroadcastRespConsumer());
        handlersC.put(BROADCAST, new BroadcastConsumer());
        handlersC.put(LEFT, new LeftConsumer());
        handlersC.put(BYE_RESP, new ByeConsumer());
        handlersC.put(HANGUP, new HangupConsumer());
        handlersC.put(JOINED, new JoinedConsumer());
        handlersC.put(SENDTO_RESP, new SendToRespConsumer());
        handlersC.put(SENDTO, new SendToConsumer());
        handlersC.put(LIST, new ListConsumer());
        handlersC.put(LIST_RESP, new ListRespConsumer());
        handlersC.put(UNKNOWN_COMMAND, new UnknownConsumer());
        handlersC.put(PARSE_ERROR, new ParseConsumer());

        // Server consumers commands that the client can send
        handlersS.put(ENTER, new EnterConsumer(clientInstance));
        handlersS.put(LIST_REQ, new ListReqConsumer(clientInstance));
        handlersS.put(BROADCAST_REQ, new BroadcastReqConsumer(clientInstance));
        handlersS.put(SENDTO_REQ, new SendToReqConsumer(clientInstance));
        handlersS.put(BYE, new ByeReqConsumer(clientInstance));
        handlersS.put(PONG, new PongConsumer(clientInstance));
    }

    /**
     * Handle message
     * This method is used to handle messages from the server
     *
     * @param command command
     * @param json json
     */
    public void handleServerMessage(ServerCommands command, String json) {
        Consumer<String> handler = handlersC.get(command);
        if (handler != null) {
            handler.accept(json);
        } else {
            System.err.println("Unknown command: " + command);
        }
    }

    public void handleClientMessage(ServerCommands command, String json) {
        Consumer<String> handler = handlersS.get(command);
        if (handler != null) {
            handler.accept(json);
        } else {
            writer.println(UNKNOWN_COMMAND);
            MessageHelper.printColoredMessage(RED, "S --> (): " + UNKNOWN_COMMAND);
        }
    }
}