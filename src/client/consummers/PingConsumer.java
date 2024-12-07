package client.consummers;

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
            System.out.println("nigga");
        } catch (Exception e) {
            System.err.println("Failed to process PING message: " + e.getMessage());
        }
    }
}