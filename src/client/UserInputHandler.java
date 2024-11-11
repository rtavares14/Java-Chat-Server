package client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class UserInputHandler implements Runnable {

    private Socket socket;

    public UserInputHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            OutputStream out = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(out);

            Scanner scanner = new Scanner(System.in);


            while (true) {
                String message = scanner.nextLine();

                //Helper menu for acoustic people who do not know the commands
                //I am the acoustic person so I need this :(
                if (message.equalsIgnoreCase("help")) {
                    helperMenu();
                    continue; // does not send "help" as a message to the server
                }

                //Paka Paka condition
                if (message.equalsIgnoreCase("BYE")) {
                    writer.println("Client has disconnected.");
                    writer.flush();
                    break;
                }

                //message to the server
                writer.println(message);
                writer.flush();
            }

            scanner.close();
            socket.close();

        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void helperMenu() {
        System.out.println("Commands:");
        System.out.println("1- ENTER {\"username\":\"<username>\"} - Login to the server");
        System.out.println("2- BROADCAST_REQ {\"message\":\"<message>\"} - Send a global broadcast message");
        System.out.println("3- BYE - Disconnect from the server");
    }
}
