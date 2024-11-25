package shared.enumerations;

public enum ServerCommands {
    LOGIN("ENTER"), // Login command
    READY("READY"), // Server is ready to accept commands
    ENTER_RESP("ENTER_RESP"), // Response to login command
    BROADCAST_REQ("BROADCAST_REQ"), // Send broadcast request
    BROADCAST_RESP("BROADCAST_RESP"), // Response to broadcast command
    BROADCAST("BROADCAST"), // Broadcast command to send message to all clients
    PING("PING"), // Ping command to check if server is alive need to respond with PONG
    PONG("PONG"), // Pong command to respond to PING
    HANGUP("HANGUP"), // Hangup command to disconnect from server - pong not received
    PONG_ERROR("PONG_ERROR"), // Error response to PING command - ping not received before pong
    BYE("BYE"), // Logout command
    BYE_RESP("BYE_RESP"), // Response to logout command
    LEFT("LEFT"), // Response to all clients when a client leaves
    PARSE_ERROR("PARSE_ERROR"), // Error parsing command received from client - invalid format
    UNKNOWN_COMMAND("UNKNOWN_COMMAND"); // Unknown command received


    private final String command;

    ServerCommands(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }

}