import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.handlers.RPSHandler;
import shared.messages.RPSGame.enter_game.GameStartReq;
import shared.messages.RPSGame.enter_game.GameStartResp;
import shared.messages.RPSGame.play_game.GameChoiceReq;
import shared.messages.RPSGame.play_game.GameChoiceResp;
import shared.messages.RPSGame.play_game.GameEnd;
import shared.messages.enter.Enter;
import shared.messages.login_logout.Joined;
import utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

public class RPSGameTests {

    private final static Properties PROPS = new Properties();
    private final static int MAX_DELTA_ALLOWED_MS = 500;
    private Process serverProcess;
    private Map<String, Socket> sockets = new HashMap<>();
    private Map<String, BufferedReader> readers = new HashMap<>();
    private Map<String, PrintWriter> writers = new HashMap<>();

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = RPSGameTests.class.getResourceAsStream("testconfig.properties");
        PROPS.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException, InterruptedException {
        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroy();
            serverProcess.waitFor();
        }
        serverProcess = new ProcessBuilder("java", "-cp", "target/classes", "server.Server").start();
        Thread.sleep(200); // Wait for the server to start
    }

    @AfterEach
    void cleanup() throws IOException, InterruptedException {
        for (Socket socket : sockets.values()) {
            socket.close();
        }
        if (serverProcess != null) {
            serverProcess.destroy();
            serverProcess.waitFor();
        }
        sockets.clear();
        readers.clear();
        writers.clear();
    }

    private void setupUser(String username) throws IOException {
        Socket socket = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        sockets.put(username, socket);
        readers.put(username, reader);
        writers.put(username, writer);

        receiveLineWithTimeout(reader); // Ready message
        writer.println(Utils.objectToMessage(new Enter(username)));
        writer.flush();
        receiveLineWithTimeout(reader); // Enter response
    }

    @Test
    public void tc58RPCGameCantPlayWithYourself() throws IOException {
        setupUser("user1");

        String requestMessage = Utils.objectToMessage(new GameStartReq("user1"));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();

        String serverResponse = receiveLineWithTimeout(readers.get("user1"));
        GameStartResp gameStartResp = Utils.messageToObject(serverResponse);

        assertEquals(new GameStartResp("ERROR", 9009), gameStartResp);
    }

    @Test
    public void tc59RPCGameRoomCreated() throws IOException, InterruptedException {
        setupUser("user1");
        setupUser("user2");

        // Send the GameStartReq from user1
        String requestMessage = Utils.objectToMessage(new GameStartReq("user2"));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();

        String serverResponse = receiveLineWithTimeout(readers.get("user1"));
        Object responseObj = Utils.messageToObject(serverResponse);

        if (responseObj instanceof Joined) {
            serverResponse = receiveLineWithTimeout(readers.get("user1"));
            responseObj = Utils.messageToObject(serverResponse);
        }

        // Assert the GameStartResp is as expected
        GameStartResp gameStartResp = (GameStartResp) responseObj;
        assertEquals(new GameStartResp("OK", null), gameStartResp);

        String user1Choice = Utils.objectToMessage(new GameChoiceReq("ROCK"));
        writers.get("user1").println(user1Choice);
        writers.get("user1").flush();

        // User2 sends the losing choice
        String user2Choice = Utils.objectToMessage(new GameChoiceReq("SCISSORS"));
        writers.get("user2").println(user2Choice);
        writers.get("user2").flush();

        Thread.sleep(1000);
    }

    @Test
    public void tc60RPCGameRoomFull() throws IOException, InterruptedException {
        setupUser("user1");
        setupUser("user2");
        setupUser("user3");

        // User1 sends a GameStartReq for user2
        String requestMessage1 = Utils.objectToMessage(new GameStartReq("user2"));
        writers.get("user1").println(requestMessage1);
        writers.get("user1").flush();

        // Wait for GameStartResp or any intermediate message for user1
        GameStartResp gameStartResp1 = waitForSpecificResponse(readers.get("user1"), GameStartResp.class);
        assertEquals(new GameStartResp("OK", null), gameStartResp1);

        // User3 tries to start a game with user2 (who is already in a game)
        String requestMessage2 = Utils.objectToMessage(new GameStartReq("user2"));
        writers.get("user3").println(requestMessage2);
        writers.get("user3").flush();

        // Wait for the GameStartResp for user3, expecting an error
        GameStartResp gameStartResp3 = waitForSpecificResponse(readers.get("user3"), GameStartResp.class);
        assertEquals(new GameStartResp("ERROR", 9001), gameStartResp3);

        String user1Choice = Utils.objectToMessage(new GameChoiceReq("ROCK"));
        writers.get("user1").println(user1Choice);
        writers.get("user1").flush();

        // User2 sends the losing choice
        String user2Choice = Utils.objectToMessage(new GameChoiceReq("SCISSORS"));
        writers.get("user2").println(user2Choice);
        writers.get("user2").flush();

        Thread.sleep(1000);
    }

    @Test
    public void tc61RPCGameUserDoesNotExist() throws IOException {
        setupUser("user1");

        String requestMessage = Utils.objectToMessage(new GameStartReq("nonexistentUser"));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();

        String serverResponse = receiveLineWithTimeout(readers.get("user1"));
        GameStartResp gameStartResp = Utils.messageToObject(serverResponse);

        assertEquals(new GameStartResp("ERROR", 6006), gameStartResp);
    }

    @Test
    public void tc62RPCGameInvalidChoiceRequest() throws IOException {
        setupUser("user1");

        String invalidChoiceMessage = Utils.objectToMessage(new GameChoiceReq("ROCK"));
        writers.get("user1").println(invalidChoiceMessage);
        writers.get("user1").flush();

        String serverResponse = receiveLineWithTimeout(readers.get("user1"));
        GameChoiceResp gameChoiceResp = Utils.messageToObject(serverResponse);

        assertEquals(new GameChoiceResp("ERROR", 9003), gameChoiceResp);
    }

    @Test
    public void tc63RPCGameInvalidChoice() throws IOException, InterruptedException {
        setupUser("user1");
        setupUser("user2");

        // Create the game room
        String gameStartRequest = Utils.objectToMessage(new GameStartReq("user2"));
        writers.get("user1").println(gameStartRequest);
        writers.get("user1").flush();

        // Send an invalid choice (not "ROCK", "PAPER", or "SCISSORS")
        String invalidChoice = Utils.objectToMessage(new GameChoiceReq("NOTPAPER"));
        writers.get("user1").println(invalidChoice);
        writers.get("user1").flush();

        receiveLineWithTimeout(readers.get("user1"));
        receiveLineWithTimeout(readers.get("user1"));
        receiveLineWithTimeout(readers.get("user1"));

        String serverResponse = receiveLineWithTimeout(readers.get("user1"));
        Object gameChoiceResp = Utils.messageToObject(serverResponse);

        // Check the type of the response object
        assertEquals(new GameChoiceResp("ERROR", 9004), gameChoiceResp);


        String user1Choice = Utils.objectToMessage(new GameChoiceReq("ROCK"));
        writers.get("user1").println(user1Choice);
        writers.get("user1").flush();

        // User2 sends the losing choice
        String user2Choice = Utils.objectToMessage(new GameChoiceReq("SCISSORS"));
        writers.get("user2").println(user2Choice);
        writers.get("user2").flush();

        Thread.sleep(1000);
    }

    @Test
    public void tc64RPCGameCannotChooseTwice() throws IOException, InterruptedException {
        setupUser("user1");
        setupUser("user2");

        // Create the game room
        writers.get("user1").println(Utils.objectToMessage(new GameStartReq("user2")));
        writers.get("user1").flush();

        // Wait for GameStartResp
        GameStartResp gameStartResp = waitForSpecificResponse(readers.get("user1"), GameStartResp.class);
        assertEquals(new GameStartResp("OK", null), gameStartResp);

        // User1 sends the first choice
        String firstChoice = Utils.objectToMessage(new GameChoiceReq("ROCK"));
        writers.get("user1").println(firstChoice);
        writers.get("user1").flush();

        // Wait for response to the first choice
        GameChoiceResp firstChoiceResp = waitForSpecificResponse(readers.get("user1"), GameChoiceResp.class);
        assertEquals(new GameChoiceResp("OK", null), firstChoiceResp);

        // User1 sends the second choice
        String secondChoice = Utils.objectToMessage(new GameChoiceReq("PAPER"));
        writers.get("user1").println(secondChoice);
        writers.get("user1").flush();

        // Wait for error response for the second choice
        GameChoiceResp secondChoiceResp = waitForSpecificResponse(readers.get("user1"), GameChoiceResp.class);
        assertEquals(new GameChoiceResp("ERROR", 9005), secondChoiceResp);

        // User2 sends a valid choice
        String user2Choice = Utils.objectToMessage(new GameChoiceReq("SCISSORS"));
        writers.get("user2").println(user2Choice);
        writers.get("user2").flush();

        Thread.sleep(1000);
    }

    @Test
    public void tc65RPCGameAnswerNotInTime() throws IOException, InterruptedException {
        setupUser("user1");
        setupUser("user2");

        // Create the game room
        writers.get("user1").println(Utils.objectToMessage(new GameStartReq("user2")));
        writers.get("user1").flush();

        // Wait for GameStartResp
        GameStartResp gameStartResp = waitForSpecificResponse(readers.get("user1"), GameStartResp.class);
        assertEquals(new GameStartResp("OK", null), gameStartResp);

        // Simulate timeout by delaying User1's choice
        Thread.sleep(12000);

        // Wait for timeout error response
        GameChoiceResp timeoutResponse = waitForSpecificResponse(readers.get("user1"), GameChoiceResp.class);
        assertEquals(new GameChoiceResp("ERROR", 9006), timeoutResponse);
    }

    @Test
    public void tc66RPCGameClientWins() throws IOException, InterruptedException {
        setupUser("user1");
        setupUser("user2");

        // Create the game room
        writers.get("user1").println(Utils.objectToMessage(new GameStartReq("user2")));
        writers.get("user1").flush();

        // Wait for GameStartResp
        GameStartResp gameStartResp = waitForSpecificResponse(readers.get("user1"), GameStartResp.class);
        assertEquals(new GameStartResp("OK", null), gameStartResp);

        // User1 sends the winning choice
        writers.get("user1").println(Utils.objectToMessage(new GameChoiceReq("ROCK")));
        writers.get("user1").flush();

        // User2 sends the losing choice
        writers.get("user2").println(Utils.objectToMessage(new GameChoiceReq("SCISSORS")));
        writers.get("user2").flush();

        // Wait for GameEnd messages
        GameEnd gameEnd1 = waitForGameEnd(readers.get("user1"));
        GameEnd gameEnd2 = waitForGameEnd(readers.get("user2"));

        // Validate the results
        assertEquals(new GameEnd("user1", "user1", "user2", "ROCK", "SCISSORS"), gameEnd1);
        assertEquals(new GameEnd("user1", "user1", "user2", "ROCK", "SCISSORS"), gameEnd2);
    }


    private GameEnd waitForGameEnd(BufferedReader reader) throws IOException {
        long timeoutMillis = 3000; // Increase timeout
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < timeoutMillis) {
            if (reader.ready()) {
                String serverResponse = reader.readLine();

                Object responseObj = Utils.messageToObject(serverResponse);
                if (responseObj instanceof GameEnd) {
                    return (GameEnd) responseObj;
                }
            }
        }
        throw new IOException("Timed out waiting for GameEnd message.");
    }

    private <T> T waitForSpecificResponse(BufferedReader reader, Class<T> expectedClass) throws IOException {
        long timeoutMillis = 3000;
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < timeoutMillis) {
            if (reader.ready()) {
                String serverResponse = reader.readLine();

                Object responseObj = Utils.messageToObject(serverResponse);
                if (expectedClass.isInstance(responseObj)) {
                    return expectedClass.cast(responseObj);
                }
            }
        }
        throw new IOException("Timed out waiting for response of type: " + expectedClass.getSimpleName());
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }
}
