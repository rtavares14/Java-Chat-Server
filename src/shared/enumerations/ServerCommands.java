package shared.enumerations;

public enum ServerCommands {
    READY("READY"), // Server is ready to accept commands
    ENTER("ENTER"), // Login command
    ENTER_RESP("ENTER_RESP"), // Response to login command
    JOINED("JOINED"), // Response to all clients when a new client joins
    LIST_REQ("LIST_REQ"), // Request a list of all login clients command
    LIST_RESP("LIST_RESP"), // Response to the list of all login clients command
    LIST("LIST"), // List of all login clients
    BROADCAST_REQ("BROADCAST_REQ"), // Send broadcast request
    BROADCAST_RESP("BROADCAST_RESP"), // Response to broadcast command
    BROADCAST("BROADCAST"), // Broadcast command to send message to all clients
    SENDTO_REQ("SENDTO_REQ"), // Send message to specific client
    SENDTO_RESP("SENDTO_RESP"), // Response to send message to specific client
    SENDTO("SENDTO"), // Message received to select client
    RPS_START_REQ("RPS_START_REQ"), // Request to start a Rock Paper Scissors game
    RPS_START_RESP("RPS_START_RESP"), // Response to start a Rock Paper Scissors game
    RPS_MSG("RPS_MSG"), // Message to start a Rock Paper Scissors game
    RPS_START("RPS_START"), // Start a Rock Paper Scissors game
    RPS_CHOICE_REQ("RPS_CHOICE_REQ"), // Request to make a Rock Paper Scissors choice
    RPS_CHOICE_RESP("RPS_CHOICE_RESP"), // Response to make a Rock Paper Scissors choice
    RPS_END("RPS_END"), // End a Rock Paper Scissors game
    ROCK("ROCK"), // Rock command
    PAPER("PAPER"), // Paper command
    SCISSORS("SCISSORS"), // Scissors command
    FILET_REQ("FILET_REQ"), // Request to send a file
    FILET_RESP("FILET_RESP"), // Response to sent to sender
    FILET("FILET"), // File message received from receiver
    FILET_ACP_REQ("FILET_ACP_REQ"), // Request to accept the file
    FILET_REJ_REQ("FILET_REJ_REQ"), // Request to reject the file
    FILET_ACP_RESP("FILET_ACP_RESP"), // Response to accept the file
    FILET_REJ_RESP("FILET_REJ_RESP"), // Response to reject the file
    FILET_REJ("FILET_REJ"), // File rejected
    FILET_START("FILET_START"), // Start sending file
    FILET_END("FILET_END"), // End sending file
    INFO("INFO"), // Info command
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