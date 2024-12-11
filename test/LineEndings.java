import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;
import shared.messages.broadcast.BroadcastReq;
import shared.messages.broadcast.BroadcastResp;
import shared.messages.enter.Enter;
import shared.messages.enter.EnterResp;
import utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class LineEndings {

    private final static Properties PROPS = new Properties();
    private Process serverProcess;
    private Socket s;
    private BufferedReader in;
    private PrintWriter out;
    private final static int MAX_DELTA_ALLOWED_MS = 500;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = LineEndings.class.getResourceAsStream("testconfig.properties");
        PROPS.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException, InterruptedException {
        // Start the server
        //import server run
        serverProcess = new ProcessBuilder("java", "java", "src/server/Server.java").start();

        // Wait for the server to start
        Thread.sleep(200);

        s = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        // Close the socket
        s.close();

        // Stop the server
        serverProcess.destroy();
    }

    @Test
    void tc21EnterFollowedByBROADCASTWithWindowsLineEndingsReturnsOk() throws JsonProcessingException {
        receiveLineWithTimeout(in); //ready message
        String message = Utils.objectToMessage(new Enter("myname")) + "\r\n" +
                Utils.objectToMessage(new BroadcastReq("a")) + "\r\n";
        out.print(message);
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        EnterResp enterResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", enterResp.status());

        serverResponse = receiveLineWithTimeout(in);
        BroadcastResp broadcastResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", broadcastResp.status());
    }

    @Test
    void tc22EnterFollowedByBROADCASTWithLinuxLineEndingsReturnsOk() throws JsonProcessingException {
        receiveLineWithTimeout(in); //ready message
        String message = Utils.objectToMessage(new Enter("myname")) + "\n" +
                Utils.objectToMessage(new BroadcastReq("a")) + "\n";
        out.print(message);
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        EnterResp enterResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", enterResp.status());

        serverResponse = receiveLineWithTimeout(in);
        BroadcastResp broadcastResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", broadcastResp.status());
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }

}