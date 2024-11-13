package client;

import utils.enumerations.ServerCommands;
import utils.ServerMessage;
import utils.enumerations.CmdColors;
import utils.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerInput implements Runnable {

    private Socket socket;

    public ServerInput(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Input stream from the socket for receiving messages
            InputStream is = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            // Print messages from the server
            String serverMessage;
            while ((serverMessage = reader.readLine()) != null) {
                ServerMessage message = parseServerMessage(serverMessage);
                handleServerMessage(message);
            }

        } catch (IOException e) {
            System.err.println("Error receiving message: " + e.getMessage());
        }
    }

    private ServerMessage parseServerMessage(String message) {
        String[] parts = message.split(" ", 2);
        ServerCommands type = ServerCommands.valueOf(parts[0]);
        String body = parts.length > 1 ? parts[1] : "";
        return new ServerMessage(type, 0, body); // Assuming code is 0 for simplicity
    }

    private void handleServerMessage(ServerMessage message) {
        switch (message.getType()) {
            case READY:
                System.out.println(CmdColors.PURPLE + ("Server is ready to receive messages.") + CmdColors.RESET);
                break;
            case ENTER_RESP:
                System.out.println(CmdColors.GREEN + "You have successfully logged in." + CmdColors.RESET);
                break;
            case BROADCAST_RESP:
                System.out.println(CmdColors.ORANGE + "Your message was sent." + CmdColors.RESET);
                break;
            case BROADCAST:
                System.out.println(CmdColors.ORANGE + message.getBody() + CmdColors.RESET);
                break;
            case LEFT:
                handleJsonMessage(message);
                break;
            default:
                System.err.println("Unknown message type: " + message.getType());
        }
    }

    private void handleJsonMessage(ServerMessage message) {
        try {
            JsonParser.parseJson(message.getBody());
        } catch (IOException e) {
            System.err.println("Error parsing JSON message: " + e.getMessage());
        }
    }
}
