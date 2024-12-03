package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import shared.enumerations.ServerCommands;
import shared.messages.*;
import shared.utils.JsonUtils;
import shared.utils.MessageHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import static shared.enumerations.CmdColors.*;
import static shared.enumerations.ServerCommands.*;

public class ClientInstance implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private static PrintWriter out;
    private BufferedReader in;
    private AtomicBoolean isRunning;
    private String username = "";
    private ClientHandler clientHandler = new ClientHandler(this);

    public ClientInstance(Socket socket, Server server) {
        this.clientSocket = socket;
        this.server = server;
        this.isRunning = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Ready ready = new Ready(server.getVersion());
            String json = JsonUtils.toJson(ready);
            sendCommand(READY, json);

            if (username == null) {
                MessageHelper.printColoredMessage(PURPLE, "S --> (): " + JsonUtils.toJson(ready));
            } else {
                MessageHelper.printColoredMessage(PURPLE, "S --> (" + username + "): " + JsonUtils.toJson(ready));
            }

            String clientUserCounts = server.getClientUserCounts();
            MessageHelper.printColoredMessage(GREEN, clientUserCounts);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                MessageHelper.printColoredMessage(GREEN, "C (" + username + ") --> S: " + inputLine);
                processMessage(inputLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(String message) {
        try {
            String[] parts = message.split(" ", 2);
            ServerCommands command = ServerCommands.valueOf(parts[0]);
            String jsonPayload = parts.length > 1 ? parts[1] : "";

            switch (command) {
                case ENTER -> handleLogin(jsonPayload);
                case BROADCAST_REQ -> handleBroadcastReq(jsonPayload);
                default -> MessageHelper.printColoredMessage(RED, "Unknown command: " + command);
            }
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
        }
    }

    private void handleLogin(String jsonPayload) throws JsonProcessingException {
        try {
            Enter enter = JsonUtils.fromJson(jsonPayload, Enter.class);
            EnterResp response = clientHandler.handleLogin(enter);
            //setUsername(enter.getUsername());
            sendCommand(ENTER_RESP, JsonUtils.toJson(response));
            MessageHelper.printColoredMessage(PURPLE, "S --> (" + username + "): " + JsonUtils.toJson(response));

        } catch (Exception e) {
            EnterResp response = new EnterResp("ERROR", 5001);
            sendCommand(ENTER_RESP, JsonUtils.toJson(response));
            System.err.println("Failed to process login message: " + e.getMessage());
        }
    }

    private void handleBroadcastReq(String jsonPayload) {
    }

    public CharSequence getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    static void sendCommand(ServerCommands command, String jsonPayload) {
        if (command != null && jsonPayload != null && !jsonPayload.isEmpty()) {
            out.println(command + " " + jsonPayload);
        } else {
            System.err.println("Invalid command or payload. Cannot send to server.");
        }
    }
}
