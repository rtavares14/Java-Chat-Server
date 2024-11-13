package utils.enumerations;

public enum ServerCommands {
    READY, // Server is ready to accept commands
    ENTER_RESP, // Response to login command
    BROADCAST_RESP, // Response to broadcast command
    PING, // Ping command to check if server is alive need to respond with PONG
    HANGUP, // Hangup command to disconnect from server - pong not received
    PONG_ERROR, // Error response to PING command - ping not received before pong
    BYE_RESP, // Response to logout command
    LEFT, // Response to all clients when a client leaves
    UNKNOWN_COMMAND, // Unknown command received
    PARSE_ERROR // Error parsing command received from client - invalid format
}