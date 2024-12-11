package shared.consummers;

import shared.messages.login_logout.Joined;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class JoinedConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            Joined message = JsonUtils.fromJson(json, Joined.class);
            MessageHelper.printColoredMessage(BLUE, message.username()+" has joined the chat!");
        } catch (Exception e) {
            System.err.println("Failed to process ENTER_RESP message: " + e.getMessage());
        }
    }
}
