package client.consumers;

import shared.messages.general.Info;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.OLIVE;

public class InfoConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            Info info = JsonUtils.fromJson(json, Info.class);
            MessageHelper.printColoredMessage(OLIVE, info.message());
        } catch (Exception e) {
            System.err.println("Failed to process INFO message: " + e.getMessage());
        }
    }
}
