package client.consumers;

import shared.enumerations.CmdColors;
import shared.utils.messages.MessageHelper;

import java.io.PrintWriter;
import java.util.function.Consumer;

import static shared.enumerations.ServerCommands.*;

public class PingConsumer implements Consumer<String> {
    private final PrintWriter writer;

    public PingConsumer(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public void accept(String json) {
        try {
            writer.println(PONG);
            //MessageHelper.printColoredMessage(CmdColors.PURPLE, "Received PING message");
        } catch (Exception e) {
            System.err.println("Failed to process PING message: " + e.getMessage());
        }
    }
}