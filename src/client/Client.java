package client;

import utils.enumerations.CmdColors;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public static final String SERVER_ADDRESS = "127.0.0.1";
    public static final int SERVER_PORT = 1337;

    public static void main(String[] args) {
        while (true) {
            try {
                //connection to the server
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                System.out.println(CmdColors.PURPLE + "Connected to server." + CmdColors.RESET);

                // start a new thread to handle user input and server messages
                new Thread(new UserInputHandler(socket)).start();
                new Thread(new ServerInput(socket)).start();
                break;

            } catch (IOException e) {
                System.err.println("Could not connect to server: HAHAHA AGAIN" );
                try {
                    // stops for 5 seconds before trying again
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    System.err.println("Sleep interrupted: " + ie.getMessage());
                }
            }
        }
    }
}