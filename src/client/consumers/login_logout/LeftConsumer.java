package client.consumers.login_logout;

import shared.messages.login_logout.Left;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.YELLOW;

public class LeftConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            Left message = JsonUtils.fromJson(json, Left.class);
            if (message.username().isEmpty()) {
                MessageHelper.printColoredMessage(YELLOW, "Someone left the chat!");
            } else {
                MessageHelper.printColoredMessage(YELLOW, message.username() + " left the chat!");
            }
        } catch (Exception e) {
            System.err.println("Failed to process LEFT message: " + e.getMessage());
        }
    }
}