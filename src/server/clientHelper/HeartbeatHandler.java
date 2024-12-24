package server.clientHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.Server;
import server.loggers.ServerLogger;
import shared.messages.ping_pong.Hangup;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.Timer;
import java.util.TimerTask;

import static shared.enumerations.CmdColors.PURPLE;
import static shared.enumerations.ServerCommands.HANGUP;
import static shared.enumerations.ServerCommands.PING;

public class HeartbeatHandler {

    private static HeartbeatHandler instance;

    /**
     * Constructor for the HeartbeatHandler class
     */
    private HeartbeatHandler() {
    }

    /**
     * Get instance
     * This method is used to get the instance of the heartbeat handler
     *
     * @return HeartbeatHandler
     */
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

    /**
     * Start heartbeat
     * This method is used to start the heartbeat
     *
     * @param clientInstance the client instance
     */
    public void startHeartbeat(ClientInstance clientInstance) {
        if (!clientInstance.isPingPongEnabled()) {
            return;
        }
        MessageHelper.printColoredMessage(PURPLE, "Starting heartbeat for " + clientInstance.getUsername());

        Timer heartbeatTimer = new Timer();

        TimerTask heartbeatTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (clientInstance) {
                    if (clientInstance.isExpectingPong()) {
                        try {
                            // Send HANGUP message
                            Hangup hangup = new Hangup(7000);
                            String json = JsonUtils.toJson(hangup);
                            clientInstance.sendCommand(HANGUP, json);
                            MessageHelper.printColoredMessage(PURPLE, "S --> C(" + clientInstance.getUsername() + ") : " + json);

                            // Cleanup the client and forcefully stop its thread
                            clientInstance.cleanup();
                            MessageHelper.printColoredMessage(PURPLE, "Client " + clientInstance.getUsername() + " has been disconnected");
                            ServerLogger.getInstance().getClientUserCounts();

                            heartbeatTimer.cancel();
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // Send PING
                        clientInstance.getOut().println(PING);
                        clientInstance.setExpectingPong(true);
                        MessageHelper.printColoredMessage(PURPLE, "S --> C(" + clientInstance.getUsername() + ") : " + PING);
                    }
                }
            }
        };

        //Schedule the heartbeat task with an initial delay of 10 seconds and repeated every 15 seconds.
        heartbeatTimer.schedule(heartbeatTask, 10000, 15000);
        //for testing purposes
        //heartbeatTimer.schedule(heartbeatTask, 1, 15);
    }
}
