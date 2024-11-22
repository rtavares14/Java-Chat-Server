package shared.utils;

import shared.enumerations.ServerCommands;
import shared.messages.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;
import static shared.enumerations.ServerCommands.*;

public class MessageHandler {
    private static final Map<ServerCommands, BiConsumer<String, PrintWriter>> handlers = new HashMap<>();
    //private final Map<ServerCommands, Consumer<Class>> handlers = new HashMap<>();

    static {
        handlers.put(PING, MessageHandler::handlePing);
        handlers.put(READY, MessageHandler::handleReady);
        handlers.put(ENTER_RESP, MessageHandler::handleEnterResp);
        handlers.put(BROADCAST_RESP, MessageHandler::handleBroadcastResp);
        handlers.put(BROADCAST, MessageHandler::handleBroadcast);
        handlers.put(LEFT, MessageHandler::handleLeft);
        handlers.put(BYE_RESP, MessageHandler::handleBye);
    }

    private static void handlePing(String string, PrintWriter printWriter) {
        try {
            printWriter.println(JsonUtils.toJson(new Pong()));
        } catch (Exception e) {
            System.err.println("Failed to process PING message: " + e.getMessage());
        }
    }

    public static void handleMessage(ServerCommands command, String json, PrintWriter writer) {
        BiConsumer<String, PrintWriter> handler = handlers.get(command);
        if (handler != null) {
            handler.accept(json, writer);
        } else {
            System.err.println("Unknown command: " + command);
        }
    }

    private static void handleReady(String json, PrintWriter writer) {
        try {
            Ready message = JsonUtils.fromJson(json, Ready.class);
            System.out.println(PURPLE + "Server is ready. Version: " + message.version() + RESET);
        } catch (Exception e) {
            System.err.println("Failed to process READY message: " + e.getMessage());
        }
    }

    private static void handleEnterResp(String json, PrintWriter writer) {
        try {
            BroadcastResp message = JsonUtils.fromJson(json, BroadcastResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                System.out.println(GREEN + "Welcome to the chat!" + RESET);
            } else {
                //here call my switch case to handle the error
                System.err.println("Enter failed. Code: " + message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process ENTER_RESP message: " + e.getMessage());
        }
    }

    private static void handleBroadcastResp(String json, PrintWriter writer) {
        try {
            BroadcastResp message = JsonUtils.fromJson(json, BroadcastResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                System.out.println(ORANGE + "Message sent!" + RESET);
            } else {
                //here call my switch case to handle the error
                System.err.println("Broadcast failed. Code: " + message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process BROADCAST_RESP message: " + e.getMessage());
        }
    }

    private static void handleBroadcast(String json, PrintWriter writer) {
        try {
            Broadcast message = JsonUtils.fromJson(json, Broadcast.class);
            System.out.println(ORANGE + message.username() + " sent :" + message.message() + RESET);
        } catch (Exception e) {
            System.err.println("Failed to process BROADCAST message: " + e.getMessage());
        }
    }

    private static void handleLeft(String string, PrintWriter printWriter) {
        try {
            Left message = JsonUtils.fromJson(string, Left.class);
            if (message.username().isEmpty()) {
                System.out.println(YELLOW + "Someone left the chat" + RESET);
            } else {
                System.out.println(YELLOW + message.username() + " left the chat" + RESET);
            }
        } catch (Exception e) {
            System.err.println("Failed to process LEFT message: " + e.getMessage());
        }
    }

    private static void handleBye(String json, PrintWriter writer) {
        try {
            BroadcastResp message = JsonUtils.fromJson(json, BroadcastResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                System.out.println(PURPLE + "Bye bye see you later!" + RESET);
            } else {
                //here call my switch case to handle the error
                System.err.println("Bye failed. Code: " + message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process BYE_RESP message: " + e.getMessage());
        }
    }
}