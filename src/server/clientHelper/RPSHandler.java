package server.clientHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.loggers.ServerLogger;
import shared.messages.general.Info;
import shared.messages.RPSGame.play_game.GameChoiceResp;
import shared.messages.RPSGame.play_game.GameEnd;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import static shared.enumerations.CmdColors.OLIVE;
import static shared.enumerations.CmdColors.RED;
import static shared.enumerations.ServerCommands.*;

public class RPSHandler implements Runnable {

    private static RPSHandler handler;
    private volatile boolean timerRunning = false;
    private ClientInstance player1 = null;
    private ClientInstance player2 = null;
    private String player1Choice = null;
    private String player2Choice = null;
    private Thread gameRoomThread;
    private boolean isGameInProgress;
    private boolean isRoomOpen;

    /**
     * Private constructor for singleton
     */
    private RPSHandler() {
    }

    /**
     * Get the singleton instance
     *
     * @return RPSHandler
     */
    public static synchronized RPSHandler getHandler() {
        if (handler == null) {
            handler = new RPSHandler();
        }
        return handler;
    }

    /**
     * Add players to the game
     *
     * @param player1 the first player
     * @param player2 the second player
     */
    public synchronized void addPlayers(ClientInstance player1, ClientInstance player2) throws JsonProcessingException {
        if (!isGameInProgress) {
            this.player1 = player1;
            this.player2 = player2;
            MessageHelper.printColoredMessage(OLIVE, "Player 1 (" + player1.getUsername() + ") and Player 2 (" + player2.getUsername() + ") have joined the game.");
            isGameInProgress = true;
            startGame();
        } else {
            MessageHelper.printColoredMessage(RED, "Game room is already full!.");
        }
    }

    /**
     * Start the game
     *
     * @throws JsonProcessingException if an error occurs while processing JSON
     */
    private void startGame() throws JsonProcessingException {
        Info info = new Info("Make your choice: ROCK, PAPER, SCISSORS");
        player1.sendCommand(INFO, JsonUtils.toJson(info));
        player2.sendCommand(INFO, JsonUtils.toJson(info));
        MessageHelper.printColoredMessage(OLIVE, "S --> ( players ): " + INFO + " " + JsonUtils.toJson(info));

        // Start a 15-second timer
        timerRunning = true; // Set the flag to true
        new Thread(() -> {
            try {
                int timeElapsed = 0;
                while (timeElapsed < 12 && timerRunning) { // Check the flag
                    Thread.sleep(1000); // Wait for 1 second
                    timeElapsed++;

                    synchronized (this) {
                        // Check if both players made their choices
                        if (player1Choice != null && player2Choice != null) {
                            determineWinner();
                            return;
                        }
                    }
                }

                // Timeout: Cancel the game
                synchronized (this) {
                    if (timerRunning && (player1Choice == null || player2Choice == null)) {
                        cancelGameDueToTimeout();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                MessageHelper.printColoredMessage(RED, "Timer interrupted.");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * Determine the winner of the game
     *
     * @throws JsonProcessingException if an error occurs while processing JSON
     */
    private synchronized void determineWinner() throws JsonProcessingException {
        // Determine the winner based on player choices
        String winner;
        if (player1Choice.equals(player2Choice)) {
            winner = null;
        } else if (
                (player1Choice.equals(ROCK.toString()) && player2Choice.equals(SCISSORS.toString())) ||
                        (player1Choice.equals(PAPER.toString()) && player2Choice.equals(ROCK.toString())) ||
                        (player1Choice.equals(SCISSORS.toString()) && player2Choice.equals(PAPER.toString()))
        ) {
            winner = player1.getUsername();
        } else {
            winner = player2.getUsername();
        }

        // Notify players of the result
        GameEnd gameEnd = new GameEnd(winner, player1.getUsername(), player2.getUsername(), player1Choice, player2Choice);
        ServerLogger.getInstance().informAllUsers(gameEnd, RPS_END, null, null);

        resetGame();
    }

    /**
     * Cancel the game due to timeout
     *
     * @throws JsonProcessingException if an error occurs while processing JSON
     */
    private synchronized void cancelGameDueToTimeout() throws JsonProcessingException {
        // Notify players of timeout
        Info timeoutInfo = new Info("Game canceled due to timeout. Please try again.");
        player1.sendCommand(INFO, JsonUtils.toJson(timeoutInfo));
        player2.sendCommand(INFO, JsonUtils.toJson(timeoutInfo));
        MessageHelper.printColoredMessage(RED, "Game canceled due to timeout.");

        GameChoiceResp response = new GameChoiceResp("ERROR", 9006);
        player1.sendCommand(RPS_CHOICE_RESP, JsonUtils.toJson(response));
        player2.sendCommand(RPS_CHOICE_RESP, JsonUtils.toJson(response));
        MessageHelper.printColoredMessage(RED, "S --> ( players ): " + RPS_CHOICE_RESP + " " + JsonUtils.toJson(response));

        // Reset the game
        resetGame();
    }


    /**
     * Check if a player is in the game
     *
     * @param player the player to check
     * @return boolean true if the player is in the game, false otherwise
     */
    public synchronized boolean isPlayerInGame(ClientInstance player) {
        return player1 == player || player2 == player;
    }

    /**
     * Add a player's choice to the game
     *
     * @param player the player
     * @param choice the player's choice
     */
    public synchronized void addChoice(ClientInstance player, String choice) {
        if (player1 == player) {
            player1Choice = choice;
        } else if (player2 == player) {
            player2Choice = choice;
        }
    }

    /**
     * Check if a player has made a choice
     *
     * @param player the player to check
     * @return boolean true if the player has made a choice, false otherwise
     */
    public boolean didPlayerMakeChoice(ClientInstance player) {
        if (player1 == player) {
            return player1Choice != null;
        } else if (player2 == player) {
            return player2Choice != null;
        }
        return false;
    }

    /**
     * Remove a players from the game
     */
    public synchronized void removePlayers() {
        isGameInProgress = false;
        player1 = null;
        player2 = null;
        player1Choice = null;
        player2Choice = null;

        MessageHelper.printColoredMessage(OLIVE, "Players have left the game.");
    }

    /**
     * Check if the game is currently in progress
     *
     * @return boolean true if the game is in progress, false otherwise
     */
    public synchronized boolean isGameInProgress() {
        return isGameInProgress;
    }

    /**
     * Clear all players and reset the game
     */
    public synchronized void resetGame() {
        timerRunning = false; // Stop the timer

        removePlayers();
        MessageHelper.printColoredMessage(OLIVE, "The game room has been reset.");
    }

    /**
     * Start the game room thread
     */
    public synchronized void startGameRoom() {
        if (isRoomOpen) {
            MessageHelper.printColoredMessage(RED, "Game room is already running.");
            return;
        }
        isRoomOpen = true;
        gameRoomThread = new Thread(this);
        gameRoomThread.start();
        MessageHelper.printColoredMessage(OLIVE, "Game room has started.");
    }

    /**
     * Stop the game room thread
     */
    public synchronized void stopGameRoom() {
        if (!isRoomOpen) {
            MessageHelper.printColoredMessage(RED, "Game room is not running.");
            return;
        }
        isRoomOpen = false;
        if (gameRoomThread != null) {
            gameRoomThread.interrupt();
        }
        MessageHelper.printColoredMessage(OLIVE, "Game room has stopped.");
    }

    /**
     * The main logic for the game room
     */
    @Override
    public void run() {
        if (isGameInProgress) {
            MessageHelper.printColoredMessage(OLIVE, "Game in progress between " +
                    player1.getUsername() + " and " + player2.getUsername());
        } else {
            MessageHelper.printColoredMessage(OLIVE, "Game room is waiting for players...");
        }
    }
}
