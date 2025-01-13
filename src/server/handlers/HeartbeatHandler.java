package server.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.clientInstance.ClientInstance;
import server.loggers.ServerLogger;
import shared.messages.login_logout.Left;
import shared.messages.ping_pong.Hangup;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static shared.enumerations.CmdColors.PURPLE;
import static shared.enumerations.ServerCommands.*;

public class HeartbeatHandler {

    private static HeartbeatHandler instance;
    private final Map<ClientInstance, Timer> clientTimers = new HashMap<>();
    private final Map<ClientInstance, TimerTask> clientTasks = new HashMap<>();

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
            instance = new HeartbeatHandler();
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

                if (clientInstance.isExpectingPong()) {
                    try {
                        // Send HANGUP message
                        Hangup hangup = new Hangup(7000);
                        String json = JsonUtils.toJson(hangup);
                        clientInstance.sendCommand(HANGUP, json);
                        MessageHelper.printServerMessage(PURPLE, clientInstance, HANGUP, json);

                        // Cleanup the client and forcefully stop its thread
                        clientInstance.cleanup();
                        MessageHelper.printColoredMessage(PURPLE, "Client " + clientInstance.getUsername() + " has been disconnected");
                        Left left = new Left(clientInstance.getUsername());
                        ServerLogger.getInstance().broadcastMessage(left, clientInstance.getUsername(), LEFT);
                        ServerLogger.getInstance().getClientUserCounts();

                        heartbeatTimer.cancel();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // Send PING
                    clientInstance.getOut().println(PING);
                    clientInstance.setExpectingPong(true);
                    MessageHelper.printServerMessage(PURPLE, clientInstance, PING, PING.toString());
                }
            }
        };

        //Schedule the heartbeat task with an initial delay of 10 seconds and repeated every 15 seconds.
        heartbeatTimer.schedule(heartbeatTask, 10000, 15000);

        // Store the timer and task for this client
        clientTimers.put(clientInstance, heartbeatTimer);
        clientTasks.put(clientInstance, heartbeatTask);
    }


    /**
     * Stop the heartbeat for a specific client
     * This method cancels the timer and task associated with the client.
     */
    public void stopHeartbeat(ClientInstance clientInstance) {
        Timer timer = clientTimers.get(clientInstance);
        TimerTask task = clientTasks.get(clientInstance);

        if (timer != null && task != null) {
            task.cancel();  // Cancel the task
            timer.cancel();  // Cancel the timer
            clientTimers.remove(clientInstance);  // Remove the client from the map
            clientTasks.remove(clientInstance);  // Remove the task from the map
            MessageHelper.printColoredMessage(PURPLE, "Heartbeat stopped for " + clientInstance.getUsername());
        }
    }

}
