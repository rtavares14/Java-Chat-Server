package client.consummers;

import shared.utils.HelperMenu;

import java.util.function.Consumer;


public class UnknownConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        HelperMenu.menu();
    }
}
