package shared.utils;

import client.consummers.PingConsumer;
import client.consummers.ReadyConsumer;
import shared.enumerations.ServerCommands;
import shared.messages.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;
import static shared.enumerations.ServerCommands.*;

public class MessageHandler {
    //private static final Map<ServerCommands, BiConsumer<String, PrintWriter>> handlers = new HashMap<>();
    //change the method to a consumer class and implemnt the accept method // implements Consumer<String>
    // override the apply method
    private final Map<ServerCommands, Consumer<String>> handlers = new HashMap<>();
    private final PrintWriter writer;

    public MessageHandler(PrintWriter writer) {
        this.writer = writer;
        handlers.put(PING, this::handlePing);
        handlers.put(READY, this::handleReady);
        handlers.put(ENTER_RESP, this::handleEnterResp);
        handlers.put(BROADCAST_RESP, this::handleBroadcastResp);
        handlers.put(BROADCAST, this::handleBroadcast);
        handlers.put(LEFT, this::handleLeft);
        handlers.put(BYE_RESP, this::handleBye);
        handlers.put(HANGUP, this::handlePongError);
    }
    //refactoring

    //evrry message
    //i need to get the ServerCoomands, split the message into command get the play payload
    //check if i have the command in the handlers map
    //if not i say paka paka
    //if i have the command i get the handler and pass the payload to it

    public void handleMessage(ServerCommands command, String json) {
        Consumer<String> handler = handlers.get(command);
        if (handler != null) {
            handler.accept(json);
        } else {
            System.err.println("Unknown command: " + command);
        }
    }

    private void handlePing(String json) {
        try {
            writer.println(PONG);
        } catch (Exception e) {
            System.err.println("Failed to process PING message: " + e.getMessage());
        }
    }

    private void handlePongError(String string) {
        System.out.println(RED + "No pong received" + RESET);
    }

    private void handleReady(String json) {
        try {
            Ready message = JsonUtils.fromJson(json, Ready.class);
            System.out.println(PURPLE + "Server is ready. Version: " + message.version() + RESET);
            System.out.println(PURPLE + "Type 'help' to see available commands" + RESET);
            System.out.println(PURPLE + "Type 'login <username>' to enter the chat" + RESET);
        } catch (Exception e) {
            System.err.println("Failed to process READY message: " + e.getMessage());
        }
    }

    private void handleEnterResp(String json) {
        try {
            BroadcastResp message = JsonUtils.fromJson(json, BroadcastResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                System.out.println(GREEN + "Welcome to the chat!" + RESET);
            } else {
                handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process ENTER_RESP message: " + e.getMessage());
        }
    }

    private void handleBroadcastResp(String json) {
        try {
            BroadcastResp message = JsonUtils.fromJson(json, BroadcastResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                System.out.println(ORANGE + "Message sent!" + RESET);
            } else {
                handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process BROADCAST_RESP message: " + e.getMessage());
        }
    }

    private void handleBroadcast(String json) {
        try {
            Broadcast message = JsonUtils.fromJson(json, Broadcast.class);
            System.out.println(ORANGE + message.username() + " sent: " + message.message() + RESET);
        } catch (Exception e) {
            System.err.println("Failed to process BROADCAST message: " + e.getMessage());
        }
    }

    private void handleLeft(String json) {
        try {
            Left message = JsonUtils.fromJson(json, Left.class);
            if (message.username().isEmpty()) {
                System.out.println(YELLOW + "Someone left the chat" + RESET);
            } else {
                System.out.println(YELLOW + message.username() + " left the chat" + RESET);
            }
        } catch (Exception e) {
            System.err.println("Failed to process LEFT message: " + e.getMessage());
        }
    }

    private void handleBye(String json) {
        try {
            BroadcastResp message = JsonUtils.fromJson(json, BroadcastResp.class);
            if ("OK".equalsIgnoreCase(message.status())) {
                System.out.println(PURPLE + "Bye bye see you later!" + RESET);
            } else {
                handleErrorMessage(message.code());
            }
        } catch (Exception e) {
            System.err.println("Failed to process BYE_RESP message: " + e.getMessage());
        }
    }

    private void handleErrorMessage(int code) {
        switch (code) {
            case 5000 -> System.out.println(RED + "User with this name already exists" + RESET);
            case 5001 -> System.out.println(RED + "Username has an invalid format or length" + RESET);
            case 5002 -> System.out.println(RED + "Already logged in" + RESET);
            case 6000 -> System.out.println(RED + "User is not logged in" + RESET);
            case 7000 -> System.out.println(RED + "No pong received" + RESET);
            case 8000 -> System.out.println(RED + "Pong without ping" + RESET);
            default -> System.out.println(RED + "Unknown error code: " + code + RESET);
        }
    }
}