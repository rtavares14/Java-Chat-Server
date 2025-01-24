import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shared.messages.RPSGame.enter_game.GameStartReq;
import shared.messages.RPSGame.enter_game.GameStartResp;
import shared.messages.RPSGame.play_game.GameEnd;
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

public class FileTransferTests {

    private final static Properties PROPS = new Properties();
    private final static int MAX_DELTA_ALLOWED_MS = 500;
    private Process serverProcess;
    private Map<String, Socket> sockets = new HashMap<>();
    private Map<String, BufferedReader> readers = new HashMap<>();
    private Map<String, PrintWriter> writers = new HashMap<>();

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = FileTransferTests.class.getResourceAsStream("testconfig.properties");
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
