import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;
import shared.messages.enter.Enter;
import shared.messages.enter.EnterResp;
import shared.messages.list.ListReq;
import shared.messages.list.ListResp;
import utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class ExtendedTests {

    private final static Properties PROPS = new Properties();
    private Process serverProcess;
    private Socket s;
    private BufferedReader in;
    private PrintWriter out;
    private final static int MAX_DELTA_ALLOWED_MS = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = AcceptedUsernamesTests.class.getResourceAsStream("testconfig.properties");
        PROPS.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException, InterruptedException {
        // Start the server
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
    void tc54NonLoginUserAskForListGetsError() throws JsonProcessingException {
        receiveLineWithTimeout(in); //ready message
        out.println(Utils.objectToMessage(new Enter("myname")));
        out.flush();
        out.println(Utils.objectToMessage(new ListReq("myname")));
        String serverResponse = receiveLineWithTimeout(in);
        ListResp listresp = Utils.messageToObject(serverResponse);
        assertEquals("OK", listresp.status());
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }
}
