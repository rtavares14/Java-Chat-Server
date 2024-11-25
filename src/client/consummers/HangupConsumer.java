package client.consummers;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class HangupConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        System.out.println(RED + "No pong received" + RESET);
    }
}