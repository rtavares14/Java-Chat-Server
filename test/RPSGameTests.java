import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.clientHelper.RPSHandler;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

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
    public void tc59RPCGameRoomCreated() throws IOException {
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
    }


    @Test
    public void tc60RPCGameRoomFull() throws IOException {
        setupUser("user1");
        setupUser("user2");
        setupUser("user3");

        String requestMessage1 = Utils.objectToMessage(new GameStartReq("user2"));
        writers.get("user1").println(requestMessage1);
        writers.get("user1").flush();

        String requestMessage2 = Utils.objectToMessage(new GameStartReq("user2"));
        writers.get("user3").println(requestMessage2);
        writers.get("user3").flush();

        String serverResponse1 = receiveLineWithTimeout(readers.get("user3"));
        GameStartResp gameStartResp1 = Utils.messageToObject(serverResponse1);
        assertEquals(new GameStartResp("ERROR", 9001), gameStartResp1);
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
        while (RPSHandler.getInstance().isGameInProgress() == true) {
            Thread.sleep(1000);
        }
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
        Object responseObj = Utils.messageToObject(serverResponse);

        // Check the type of the response object
        if (responseObj instanceof GameChoiceResp gameChoiceResp) {
            assertEquals(new GameChoiceResp("ERROR", 9004), gameChoiceResp);
            System.out.println("pintou ?");
        }
    }

    @Test
    public void tc64RPCGameCannotChooseTwice() throws IOException, InterruptedException {
        while (RPSHandler.getInstance().isGameInProgress() == true) {
            Thread.sleep(1000);
            System.out.println("pintou ?");
        }
        setupUser("user1");
        setupUser("user2");

        // Create the game room
        String gameStartRequest = Utils.objectToMessage(new GameStartReq("user2"));
        writers.get("user1").println(gameStartRequest);
        writers.get("user1").flush();

        // User1 sends the first choice
        String firstChoice = Utils.objectToMessage(new GameChoiceReq("ROCK"));
        writers.get("user1").println(firstChoice);
        writers.get("user1").flush();

        // Skip the valid choice response
        receiveLineWithTimeout(readers.get("user1"));
        receiveLineWithTimeout(readers.get("user1"));
        receiveLineWithTimeout(readers.get("user1"));
        receiveLineWithTimeout(readers.get("user1"));

        // User1 sends the second choice
        String secondChoice = Utils.objectToMessage(new GameChoiceReq("PAPER"));
        writers.get("user1").println(secondChoice);
        writers.get("user1").flush();

        String serverResponse = receiveLineWithTimeout(readers.get("user1"));
        Object responseObj = Utils.messageToObject(serverResponse);

        // Check the type of the response object
        if (responseObj instanceof GameChoiceResp gameChoiceResp) {
            assertEquals(new GameChoiceResp("ERROR", 9005), gameChoiceResp);
            System.out.println("pintou ?");
        }
    }

    @Test
    public void tc65RPCGameAnswerNotInTime() throws IOException, InterruptedException {
        while (RPSHandler.getInstance().isGameInProgress() == true) {
            Thread.sleep(1000);
            System.out.println("pintou ?");
        }
        setupUser("user1");
        setupUser("user2");

        // Create the game room
        String gameStartRequest = Utils.objectToMessage(new GameStartReq("user2"));
        writers.get("user1").println(gameStartRequest);
        writers.get("user1").flush();

        // Simulate delay of more than 20 seconds before sending choice
        Thread.sleep(14000);

        String serverResponse = receiveLineWithTimeout(readers.get("user1"));
        Object responseObj = Utils.messageToObject(serverResponse);

        // Check the type of the response object
        if (responseObj instanceof GameChoiceResp gameChoiceResp) {
            System.out.println(gameChoiceResp);
            assertEquals(new GameChoiceResp("ERROR", 9006), gameChoiceResp);
            System.out.println("pintou ?");
        }
    }

    @Test
    public void tc66RPCGameClientWins() throws IOException, InterruptedException {
        while (RPSHandler.getInstance().isGameInProgress() == true) {
            Thread.sleep(1000);
            System.out.println("pintou ?");
        }
        setupUser("user1");
        setupUser("user2");

        // Create the game room
        String gameStartRequest = Utils.objectToMessage(new GameStartReq("user2"));
        writers.get("user1").println(gameStartRequest);
        writers.get("user1").flush();

        // Skip intermediate messages (JOINED and GameStartResp)

        // User1 sends the winning choice
        String user1Choice = Utils.objectToMessage(new GameChoiceReq("ROCK"));
        writers.get("user1").println(user1Choice);
        writers.get("user1").flush();

        // User2 sends the losing choice
        String user2Choice = Utils.objectToMessage(new GameChoiceReq("SCISSORS"));
        writers.get("user2").println(user2Choice);
        writers.get("user2").flush();

        // Receive the game end response
        String serverResponse1 = receiveLineWithTimeout(readers.get("user1"));
        Object responseObj1 = Utils.messageToObject(serverResponse1);

        String serverResponse2 = receiveLineWithTimeout(readers.get("user2"));
        Object responseObj2 = Utils.messageToObject(serverResponse2);

        // Check the type of the response object
        if (responseObj1 instanceof GameChoiceResp gameEnd1) {
            assertEquals(new GameEnd("user1", "user1", "user2", "ROCK", "SCISSORS"), gameEnd1);
            System.out.println("pintou ?");
        }

        if (responseObj2 instanceof GameChoiceResp gameEnd2) {
            assertEquals(new GameEnd("user1", "user1", "user2", "ROCK", "SCISSORS"), gameEnd2);
            System.out.println("pintou ?");
        }
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }
}
