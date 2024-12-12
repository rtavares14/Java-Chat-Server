package server.consumers;

import server.clientHelper.ClientInstance;

public class PongConsumer implements java.util.function.Consumer<String> {
    private final ClientInstance clientInstance;

    public PongConsumer(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void accept(String json) {
    }
}
