package shared.utils.messages;

import server.clientInstance.ClientInstance;
import shared.enumerations.CmdColors;
import shared.enumerations.ServerCommands;

import static shared.enumerations.CmdColors.*;

public class MessageHelper {

    /**
     * Prints a message in the specified color and then resets the color.
     *
     * @param color   the color to print the message in
     * @param message the message to be printed
     */
    public static void printColoredMessage(CmdColors color, String message) {
        System.out.println(color + message + RESET);
    }

    /**
     * Prints a message in the specified color and then resets the color.
     *
     * @param color   the color to print the message in
     * @param clientInstance the client instance
     * @param command the command to be printed
     * @param response the response to be printed
     */
    public static void printServerMessage(CmdColors color, ClientInstance clientInstance, ServerCommands command, String response) {
        printColoredMessage(color, "S --> (" + clientInstance.getUsername() + "): " + command + " " + response);
    }

    /**
     * Display the help menu
     */
    public static void menu() {
        printColoredMessage(PURPLE, "-----------------------------COMMANDS-----------------------------");
        printColoredMessage(PURPLE, "LOGIN \"username\" - Login to the server");
        printColoredMessage(PURPLE, "ULIST - Request a list of all login users");
        printColoredMessage(PURPLE, "MSG \"message\" - Send a global broadcast message");
        printColoredMessage(PURPLE, "PVM \"username\" \"message\" - Send a private message to a user");
        printColoredMessage(PURPLE, "PLAY \"username\" - Send a play request to a user");
        printColoredMessage(PURPLE, "In game commands: rock, paper or scissors");
        printColoredMessage(PURPLE, "FILET \"username\" \"file-path\" - Send a file to a user");
        printColoredMessage(PURPLE, "ACCEPT OR DECLINE \"username\" \"uuid\"- Accept a file transfer");
        printColoredMessage(PURPLE, "HELP - Display this help menu");
        printColoredMessage(PURPLE, "BYE - Disconnect from the server");
        printColoredMessage(PURPLE, "------------------------------------------------------------------");
        printColoredMessage(PURPLE, "Disclaimer: Any command can be written in lower or upper case.");
        printColoredMessage(PURPLE, "------------------------------------------------------------------");
    }

    /**
     * Handle error messages
     * This method is used to handle error messages from the server
     * This method needs to be STATIC because it is called from the other class
     * The handleErrorMessage method serves as a shared utility function for error handling across multiple consumers.
     * Making it static allows it to be easily accessed and used without requiring an instance of the MessageHandler.
     *
     * @param code error code
     */
    public static void handleErrorMessage(int code) {
        switch (code) {
            case 5000 -> MessageHelper.printColoredMessage(RED, "User with this name already exists!");
            case 5001 -> MessageHelper.printColoredMessage(RED, "Username has an invalid format or length!");
            case 5002 -> MessageHelper.printColoredMessage(RED, "Already logged in!");
            case 6000 -> MessageHelper.printColoredMessage(RED, "Not logged in. Use login <\"username\">!");
            case 6006 -> MessageHelper.printColoredMessage(RED, "User is not login atm!");
            case 7000 -> MessageHelper.printColoredMessage(RED, "No pong received!");
            case 8000 -> MessageHelper.printColoredMessage(RED, "Pong without ping!");
            case 9000 -> MessageHelper.printColoredMessage(RED, "Already playing in a game!");
            case 9001 -> MessageHelper.printColoredMessage(RED, "Game room is full!");
            case 9003 -> MessageHelper.printColoredMessage(RED, "Not playing the game!");
            case 9004 -> MessageHelper.printColoredMessage(RED, "Not rock paper or scissors!");
            case 9005 -> MessageHelper.printColoredMessage(RED, "Already made a choice!");
            case 9006 -> MessageHelper.printColoredMessage(RED, "Not all players made a choice!");
            case 9009 -> MessageHelper.printColoredMessage(RED, "You cant play with yourself!");
            case 10000 -> MessageHelper.printColoredMessage(RED, "You cant send a file to yourself!");
            case 10001 -> MessageHelper.printColoredMessage(RED, "File not found!");
            case 10002 -> MessageHelper.printColoredMessage(RED, "Sender rejected the file!");
            case 10003 -> MessageHelper.printColoredMessage(RED, "Sender left the server!");
            case 10004 -> MessageHelper.printColoredMessage(RED, "Receiver left the server!");
            case 10005 -> MessageHelper.printColoredMessage(RED, "Checksum failed!");
            case 10006 -> MessageHelper.printColoredMessage(RED, "File transfer not found! Wrong ID!");
            case 10007 -> MessageHelper.printColoredMessage(RED, "This user did not send a file to you!");
            case 10008 -> MessageHelper.printColoredMessage(RED, "!");
            case 10009 -> MessageHelper.printColoredMessage(RED, "Receiver did not respond in time!");
            case 10010 -> MessageHelper.printColoredMessage(RED, "You did not answer in time! File transfer cancelled!");
            case 10011 -> MessageHelper.printColoredMessage(RED, "File transfer already accepted!");
            default -> MessageHelper.printColoredMessage(RED, "Unknown error code: " + code);
        }
    }
}
