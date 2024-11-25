package client.consummers;

import shared.messages.Ready;
import shared.utils.JsonUtils;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class ReadyConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            Ready message = JsonUtils.fromJson(json, Ready.class);
            System.out.println(PURPLE + "Server is ready. Version: " + message.version() + RESET);
            System.out.println(PURPLE + "Type 'help' to see available commands" + RESET);
            System.out.println(PURPLE + "Type 'login <username>' to enter the chat" + RESET);
        } catch (Exception e) {
            System.err.println("Failed to process READY message: " + e.getMessage());
        }
    }
}
