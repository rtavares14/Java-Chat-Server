package client.consumers.general;

import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;


public class UnknownConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        MessageHelper.menu();
    }
}
