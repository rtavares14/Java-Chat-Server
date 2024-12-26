package client.consumers;

import shared.messages.Info;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.OLIVE;

public class InfoConsumer implements Consumer<String> {

    @Override
    public void accept(String jsonPayload) {
        try {
            Info info = JsonUtils.fromJson(jsonPayload, Info.class);
            MessageHelper.printColoredMessage(OLIVE, info.message());
        } catch (Exception e) {
            System.err.println("Failed to process INFO message: " + e.getMessage());
        }
    }
}
