package shared.utils;

import shared.enumerations.CmdColors;
import static shared.enumerations.CmdColors.*;

public class MessageWriter {

    /**
     * Prints a message in the specified color and then resets the color.
     *
     * @param color  the color to print the message in
     * @param message the message to be printed
     */
    public static void printColoredMessage(CmdColors color, String message) {
        System.out.println(color + message + RESET);
    }
}
