package shared.utils;

import shared.enumerations.CmdColors;

public class HelperMenu {

    /**
     * Display the help menu
     */
    public static void menu() {
        System.out.println(CmdColors.PURPLE + "Commands:");
        System.out.println("login \"username\" - Login to the server");
        System.out.println("ulist - Request a list of all login users");
        System.out.println("msg \"message\" - Send a global broadcast message");
        System.out.println("pvm \"username\" \"message\" - Send a private message to a user");
        System.out.println("help - Display this help menu");
        System.out.println("bye - Disconnect from the server" + CmdColors.RESET);
    }
}
