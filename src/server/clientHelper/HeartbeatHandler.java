package server.clientHelper;

import shared.utils.messages.MessageHelper;

import java.util.Timer;
import java.util.TimerTask;

import static shared.enumerations.CmdColors.PURPLE;
import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.PING;

public class HeartbeatHandler {

    private static HeartbeatHandler instance;

    private HeartbeatHandler() {
    }

    public static HeartbeatHandler getInstance() {
        if (instance == null) {
            synchronized (HeartbeatHandler.class) {
                if (instance == null) {
                    instance = new HeartbeatHandler();
                }
            }
        }
        return instance;
    }

    public void startHeartbeat(ClientInstance clientInstance) {
        if (!clientInstance.isPingPongEnabled()) {
            return;
        }
            MessageHelper.printColoredMessage(PURPLE, "Starting heartbeat for " + clientInstance.getUsername());

            Timer heartbeatTimer = new Timer();
            clientInstance.setHeartbeatTimer(heartbeatTimer);

            TimerTask heartbeatTask = new TimerTask() {
                @Override
                public void run() {
                    synchronized (clientInstance) {
                        if (clientInstance.isExpectingPong()) {
                            MessageHelper.printColoredMessage(RED, "Client " + clientInstance.getUsername() + " did not respond to PING. Disconnecting client.");
                            clientInstance.cleanup();
                            heartbeatTimer.cancel();
                        } else {
                            clientInstance.getOut().println(PING);
                            clientInstance.setExpectingPong(true);
                        }
                    }
                }
            };

            // Schedule the heartbeat task with an initial delay of 0 and repeated every 15 seconds.
            heartbeatTimer.schedule(heartbeatTask, 0, 15000);
    }
}
