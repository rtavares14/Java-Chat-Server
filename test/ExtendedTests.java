import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shared.messages.enter.Enter;
import shared.messages.enter.EnterResp;
import shared.messages.list.ListReq;
import shared.messages.list.ListResp;
import shared.messages.private_message.SendTo;
import shared.messages.private_message.SendToReq;
import shared.messages.private_message.SendToResp;
import utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class ExtendedTests {

    private final static Properties PROPS = new Properties();
    private final static int MAX_DELTA_ALLOWED_MS = 500;
    private Process serverProcess;
    private Socket socketUser1, socketUser2;
    private BufferedReader inUser1, inUser2;
    private PrintWriter outUser1, outUser2;

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

        socketUser1 = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        inUser1 = new BufferedReader(new InputStreamReader(socketUser1.getInputStream()));
        outUser1 = new PrintWriter(socketUser1.getOutputStream(), true);

        socketUser2 = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        inUser2 = new BufferedReader(new InputStreamReader(socketUser2.getInputStream()));
        outUser2 = new PrintWriter(socketUser2.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        // Close the sockets
        socketUser1.close();
        socketUser2.close();

        // Stop the server
        serverProcess.destroy();
    }

    @Test
    void tc54NonLoginUserAskForListGetsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser2); // ready message
        outUser2.println(Utils.objectToMessage(new ListReq("myname")));
        outUser2.flush();
        String serverResponse = receiveLineWithTimeout(inUser2);
        ListResp listresp = Utils.messageToObject(serverResponse);
        assertEquals("ERROR", listresp.status());
    }

    @Test
    void tc55LoginUserAskForListGetsList() throws JsonProcessingException {
        receiveLineWithTimeout(inUser2); // ready message
        outUser2.println(Utils.objectToMessage(new Enter("testuser")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); // enter response

        outUser2.println(Utils.objectToMessage(new ListReq("testuser")));
        outUser2.flush();
        String serverResponse = receiveLineWithTimeout(inUser2);
        ListResp listresp = Utils.messageToObject(serverResponse);
        assertEquals("OK", listresp.status());
    }

    @Test
    void tc56SendPrivateMessage() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready message
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); // enter response

        receiveLineWithTimeout(inUser2); // ready message
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); // enter response

        // Send private message from user1 to user2
        outUser1.println(Utils.objectToMessage(new SendToReq("user2", "Hello user2!")));
        outUser1.flush();
        String serverResponse = receiveLineWithTimeout(inUser1);
        if (serverResponse.startsWith("JOINED")) {
            serverResponse = receiveLineWithTimeout(inUser1); // skip the JOINED message
        }
        SendToResp sendToResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", sendToResp.status());

        // Verify user2 received the message
        String privateMessage = receiveLineWithTimeout(inUser2);
        SendTo sendTo = Utils.messageToObject(privateMessage);
        assertEquals("user1", sendTo.username());
        assertEquals("Hello user2!", sendTo.message());
    }

    @Test
    void tc57SendPrivateMessage() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // ready message
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); // enter response

        // Send private message from user1 to user2
        outUser1.println(Utils.objectToMessage(new SendToReq("user3", "Hello user3!")));
        outUser1.flush();
        String serverResponse = receiveLineWithTimeout(inUser1);
        if (serverResponse.startsWith("JOINED")) {
            serverResponse = receiveLineWithTimeout(inUser1); // skip the JOINED message
        }
        SendToResp sendToResp = Utils.messageToObject(serverResponse);
        assertEquals("ERROR", sendToResp.status());
        assertEquals(6006, sendToResp.code());
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }
}