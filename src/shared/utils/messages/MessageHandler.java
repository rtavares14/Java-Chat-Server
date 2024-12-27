package shared.utils.messages;

import client.consumers.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import server.clientHelper.ClientInstance;
import server.consumers.*;
import shared.enumerations.ServerCommands;
import shared.messages.errors.ParseError;
import shared.utils.JsonUtils;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static shared.enumerations.CmdColors.PURPLE;
import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.*;

public class MessageHandler {
    private final Map<ServerCommands, Consumer<String>> handlersC = new HashMap<>();
    private final Map<ServerCommands, Consumer<String>> handlersS = new HashMap<>();
    private final PrintWriter writer;

    /**
     * Constructor
     * MessageHandler constructor that initializes the writer
     * handlers map with the appropriate consumers
     *
     * @param writer writer
     */
    public MessageHandler(PrintWriter writer, ClientInstance clientInstance) {
        this.writer = writer;

        // User consumers commands that the server can send

        handlersC.put(READY, new ReadyConsumer());
        handlersC.put(ENTER_RESP, new EnterRespConsumer());
        handlersC.put(JOINED, new JoinedConsumer());
        handlersC.put(BROADCAST_RESP, new BroadcastRespConsumer());
        handlersC.put(BROADCAST, new BroadcastConsumer());
        handlersC.put(SENDTO_RESP, new SendToRespConsumer());
        handlersC.put(SENDTO, new SendToConsumer());
        handlersC.put(LIST, new ListConsumer());
        handlersC.put(LIST_RESP, new ListRespConsumer());
        handlersC.put(RPS_START_RESP, new RPSStartRespConsumer());
        handlersC.put(RPS_MSG, new RPSMsgConsumer());
        handlersC.put(RPS_CHOICE_RESP, new RPSChoiceRespConsumer());
        handlersC.put(RPS_END, new RPSEndConsumer());
        handlersC.put(PING, new PingConsumer(writer));
        handlersC.put(HANGUP, new HangupConsumer());
        handlersC.put(UNKNOWN_COMMAND, new UnknownConsumer());
        handlersC.put(PARSE_ERROR, new ParseConsumer());
        handlersC.put(BYE_RESP, new ByeConsumer());
        handlersC.put(LEFT, new LeftConsumer());
        handlersC.put(INFO, new InfoConsumer());

        // Server consumers commands that the client can send
        handlersS.put(ENTER, new EnterConsumer(clientInstance));
        handlersS.put(BROADCAST_REQ, new BroadcastReqConsumer(clientInstance));
        handlersS.put(SENDTO_REQ, new SendToReqConsumer(clientInstance));
        handlersS.put(LIST_REQ, new ListReqConsumer(clientInstance));
        handlersS.put(RPS_START_REQ, new RPSStartReqConsumer(clientInstance));

        handlersS.put(RPS_CHOICE_REQ, new RPSChoiseReqConsumer(clientInstance));
        handlersS.put(PONG, new PongConsumer(clientInstance));
        handlersS.put(BYE, new ByeReqConsumer(clientInstance));
    }

    /**
     * Handle message
     * This method is used to handle messages from the server
     *
     * @param command command
     * @param json    json
     */
    public void handleServerMessage(ServerCommands command, String json) {
        Consumer<String> handler = handlersC.get(command);
        if (handler != null) {
            handler.accept(json);
        } else {
            System.err.println("Unknown command: " + command);
        }
    }

    /**
     * Handle client message
     * This method is used to handle messages from the client
     *
     * @param command command
     * @param json    json
     * @throws JsonProcessingException JsonProcessingException
     */
    public void handleClientMessage(ServerCommands command, String json) throws JsonProcessingException {
        Consumer<String> handler = handlersS.get(command);

        if (handler != null) {
            // Check if the command requires a body
            if (json != null && !json.trim().isEmpty()) {
                // Validate the JSON payload
                try {
                    JsonUtils.fromJson(json, Object.class); // Ensure valid JSON
                } catch (JsonProcessingException e) {
                    // JSON is invalid, send PARSING_ERROR
                    writer.println(PARSE_ERROR); // Send PARSE_ERROR
                    //MessageHelper.printColoredMessage(RED, "S --> (123): " + PARSE_ERROR);
                    return;
                }
            }

            // If everything is valid, process the command
            handler.accept(json);

        } else {
            // If no handler exists for the command, send UNKNOWN_COMMAND
            writer.println(UNKNOWN_COMMAND);
            //MessageHelper.printColoredMessage(RED, "S --> (123): " + UNKNOWN_COMMAND);
        }
    }
}