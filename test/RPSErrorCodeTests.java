import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shared.messages.RPSGame.enter_game.GameStartReq;
import shared.messages.RPSGame.enter_game.GameStartResp;
import shared.messages.RPSGame.play_game.GameChoiceReq;
import shared.messages.RPSGame.play_game.GameChoiceResp;
import shared.messages.enter.Enter;
import utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class RPSErrorCodeTests {

    private final static Properties PROPS = new Properties();
    private final static int MAX_DELTA_ALLOWED_MS = 500;
    private Process serverProcess;
    private Map<String, Socket> sockets = new HashMap<>();
    private Map<String, BufferedReader> readers = new HashMap<>();
    private Map<String, PrintWriter> writers = new HashMap<>();

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = RPSErrorCodeTests.class.getResourceAsStream("testconfig.properties");
        PROPS.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException, InterruptedException {
        serverProcess = new ProcessBuilder("java", "java", "src/server/Server.java").start();
        Thread.sleep(200); // Wait for the server to start
    }

    @AfterEach
    void cleanup() throws IOException {
        for (Socket socket : sockets.values()) {
            socket.close();
        }
        serverProcess.destroy();
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

        String requestMessage = Utils.objectToMessage(new GameStartReq("user2"));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();

        String serverResponse = receiveLineWithTimeout(readers.get("user1"));
        Object responseObj = Utils.messageToObject(serverResponse);

        if (responseObj instanceof GameStartResp) {
            GameStartResp gameStartResp = (GameStartResp) responseObj;
            assertEquals(new GameStartResp("OK", null), gameStartResp);
        } else {
            throw new AssertionError("Expected GameStartResp but received: " + responseObj.getClass().getSimpleName());
        }
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

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }
}
