package shared.utils;

import client.consummers.*;
import shared.enumerations.ServerCommands;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;
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
        handlers.put(ENTER_RESP, new EnterConsumer());
        handlers.put(BROADCAST_RESP, new BroadcastRespConsumer());
        handlers.put(BROADCAST, new BroadcastConsumer());
        handlers.put(LEFT, new LeftConsumer());
        handlers.put(BYE_RESP, new ByeConsumer());
        handlers.put(HANGUP, new HangupConsumer());
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

    /**
     * Handle error messages
     * This method is used to handle error messages from the server
     * This method needs to be STATIC because it is called from the other class
     * The handleErrorMessage method serves as a shared utility function for error handling across multiple consumers.
     * Making it static allows it to be easily accessed and used without requiring an instance of the MessageHandler.
     *
     * @param code error code
     */
    public static void handleErrorMessage(int code) {
        switch (code) {
            case 5000 -> MessageWriter.printColoredMessage(RED,"User with this name already exists" );
            case 5001 -> MessageWriter.printColoredMessage(RED,"Username has an invalid format or length");
            case 5002 -> MessageWriter.printColoredMessage(RED,"Already logged in");
            case 6000 -> MessageWriter.printColoredMessage(RED,"User is not logged in");
            case 7000 -> MessageWriter.printColoredMessage(RED,"No pong received" );
            case 8000 -> MessageWriter.printColoredMessage(RED,"Pong without ping");
            default -> MessageWriter.printColoredMessage(RED,"Unknown error code: " + code);
        }
    }
}