package shared.utils.messages;

import shared.enumerations.CmdColors;

import static shared.enumerations.CmdColors.*;
import static shared.enumerations.ServerCommands.*;

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
     * Display the help menu
     */
    public static void menu() {
        printColoredMessage(PURPLE, "Commands:");
        printColoredMessage(PURPLE, "login \"username\" - Login to the server");
        printColoredMessage(PURPLE, "ulist - Request a list of all login users");
        printColoredMessage(PURPLE, "msg \"message\" - Send a global broadcast message");
        printColoredMessage(PURPLE, "pvm \"username\" \"message\" - Send a private message to a user");
        printColoredMessage(PURPLE, "play \"username\" - Send a play request to a user");
        printColoredMessage(PURPLE, "In game commands: " + ROCK + "," + PAPER + " or " + SCISSORS);
        printColoredMessage(PURPLE, "help - Display this help menu");
        printColoredMessage(PURPLE, "bye - Disconnect from the server");
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
            case 6000 -> MessageHelper.printColoredMessage(RED, "User is not logged in!");
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
            default -> MessageHelper.printColoredMessage(RED, "Unknown error code: " + code);
        }
    }
}
