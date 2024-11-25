package client.consummers;

import shared.messages.Left;
import shared.utils.JsonUtils;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class LeftConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
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
}