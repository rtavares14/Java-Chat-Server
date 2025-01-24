import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shared.messages.enter.Enter;
import shared.messages.file_transfer.choises.FileTransferACPReq;
import shared.messages.file_transfer.choises.FileTransferACPResp;
import shared.messages.file_transfer.choises.FileTransferRJReq;
import shared.messages.file_transfer.choises.FileTransferRJResp;
import shared.messages.file_transfer.request.FileTransfer;
import shared.messages.file_transfer.request.FileTransferReq;
import shared.messages.file_transfer.request.FileTransferResp;
import utils.Utils;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
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
    private String filePath = "test_resources/testfile.txt";
    private File file = new File(filePath);

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

        readers.clear();
        writers.clear();
        sockets.clear();
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
    public void tc67FILETCantSendFileToYourSelf() throws IOException, NoSuchAlgorithmException {
        setupUser("user1");

        String requestMessage = Utils.objectToMessage(new FileTransferReq("user1", filePath, Utils.getFileSize(filePath), Utils.createChecksum(String.valueOf(filePath))));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();

        String serverResponse = receiveLineWithTimeout(readers.get("user1"));
        FileTransferResp fileTransferRespResp = Utils.messageToObject(serverResponse);

        assertEquals(new FileTransferResp("ERROR", 10000), fileTransferRespResp);
    }

    @Test
    public void tc68FILETCantSendToNotLoginUser() throws IOException, NoSuchAlgorithmException {
        setupUser("user1");

        String requestMessage = Utils.objectToMessage(new FileTransferReq("user2", filePath, Utils.getFileSize(filePath), Utils.createChecksum(String.valueOf(filePath))));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();

        String serverResponse = receiveLineWithTimeout(readers.get("user1"));
        FileTransferResp fileTransferRespResp = Utils.messageToObject(serverResponse);

        assertEquals(new FileTransferResp("ERROR", 6006), fileTransferRespResp);
    }

    @Test
    public void tc69FILETFileNotFound() throws IOException, NoSuchAlgorithmException {
        setupUser("user1");
        setupUser("user2");

        String requestMessage = Utils.objectToMessage(new FileTransferReq("user2", "", 0.0, ""));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();
        writers.get("user2").flush();

        FileTransferResp fileTransferRespResp = receiveLineWithTimeout(readers.get("user1"), FileTransferResp.class);

        assertEquals(new FileTransferResp("ERROR", 10001), fileTransferRespResp);
    }


    @Test
    public void tc70FILETGetRequest() throws IOException, NoSuchAlgorithmException {
        setupUser("user1");
        setupUser("user2");

        String requestMessage = Utils.objectToMessage(new FileTransferReq("user2", filePath, Utils.getFileSize(filePath), Utils.createChecksum(String.valueOf(filePath))));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();
        writers.get("user2").flush();

        FileTransfer fileTransfer = receiveLineWithTimeout(readers.get("user2"), FileTransfer.class);

        assertEquals(new FileTransfer("user1", file.getName(), Utils.getFileSize(filePath), Utils.createChecksum(String.valueOf(filePath)), fileTransfer.uuid()), fileTransfer);
    }

    @Test
    public void tc71FILETAcceptRequest() throws IOException, NoSuchAlgorithmException {
        setupUser("user1");
        setupUser("user2");

        String requestMessage = Utils.objectToMessage(new FileTransferReq("user2", filePath, Utils.getFileSize(filePath), Utils.createChecksum(filePath)));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();

        FileTransfer fileTransfer = receiveLineWithTimeout(readers.get("user2"), FileTransfer.class);

        assertEquals(new FileTransfer("user1", file.getName(), Utils.getFileSize(filePath), Utils.createChecksum(filePath), fileTransfer.uuid()), fileTransfer);

        String acceptMessage = Utils.objectToMessage(new FileTransferACPReq("user1", fileTransfer.uuid()));
        writers.get("user2").println(acceptMessage);
        writers.get("user2").flush();

        FileTransferACPResp fileTransferACPResp = receiveLineWithTimeout(readers.get("user2"), FileTransferACPResp.class);

        assertEquals(new FileTransferACPResp("OK", null), fileTransferACPResp);
    }


    @Test
    public void tc72FILETARejectRequest() throws IOException, NoSuchAlgorithmException {
        setupUser("user1");
        setupUser("user2");

        String requestMessage = Utils.objectToMessage(new FileTransferReq("user2", filePath, Utils.getFileSize(filePath), Utils.createChecksum(filePath)));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();

        FileTransfer fileTransfer = receiveLineWithTimeout(readers.get("user2"), FileTransfer.class);

        assertEquals(new FileTransfer("user1", file.getName(), Utils.getFileSize(filePath), Utils.createChecksum(filePath), fileTransfer.uuid()), fileTransfer);

        String rejectMessage = Utils.objectToMessage(new FileTransferRJReq("user1", fileTransfer.uuid()));
        writers.get("user2").println(rejectMessage);
        writers.get("user2").flush();

        FileTransferRJResp fileTransferRJResp = receiveLineWithTimeout(readers.get("user2"), FileTransferRJResp.class);
        assertEquals(new FileTransferRJResp("OK", null), fileTransferRJResp); // Assuming 10008 is the success code for rejection response

        FileTransferResp fileTransferResp = receiveLineWithTimeout(readers.get("user1"), FileTransferResp.class);
        assertEquals(new FileTransferResp("OK", null), fileTransferResp); // Assuming 10009 is the error code for rejection notification
    }


    @Test
    public void tc73FILETARejectRequestTimeOut() throws IOException, NoSuchAlgorithmException, InterruptedException {
        setupUser("user1");
        setupUser("user2");

        String requestMessage = Utils.objectToMessage(new FileTransferReq("user2", filePath, Utils.getFileSize(filePath), Utils.createChecksum(filePath)));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();
        
        FileTransfer fileTransfer = receiveLineWithTimeout(readers.get("user2"), FileTransfer.class);
        assertEquals(new FileTransfer("user1", file.getName(), Utils.getFileSize(filePath), Utils.createChecksum(filePath), fileTransfer.uuid()), fileTransfer);
        
        Thread.sleep(15000); // 15 seconds timeout

        // User1 receives a timeout response
        FileTransferRJResp user1TimeoutResp = receiveLineWithTimeout(readers.get("user1"), FileTransferRJResp.class);
        assertEquals(new FileTransferRJResp("ERROR", 10009), user1TimeoutResp);

        // User2 receives a timeout response
        FileTransferRJResp user2TimeoutResp = receiveLineWithTimeout(readers.get("user2"), FileTransferRJResp.class);
        assertEquals(new FileTransferRJResp("ERROR", 10010), user2TimeoutResp);
    }


    @Test
    public void tc81FILETAcceptRequestWrongUUID() throws IOException, NoSuchAlgorithmException {
        setupUser("user1");
        setupUser("user2");

        String requestMessage = Utils.objectToMessage(new FileTransferReq("user2", filePath, Utils.getFileSize(filePath), Utils.createChecksum(String.valueOf(filePath))));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();
        writers.get("user2").flush();

        FileTransfer fileTransfer = receiveLineWithTimeout(readers.get("user2"), FileTransfer.class);
        assertEquals(new FileTransfer("user1", file.getName(), Utils.getFileSize(filePath), Utils.createChecksum(String.valueOf(filePath)), fileTransfer.uuid()), fileTransfer);

        String acceptMessage = Utils.objectToMessage(new FileTransferACPReq("user1", "fileTransferuuid"));
        writers.get("user2").println(acceptMessage);
        writers.get("user2").flush();
        writers.get("user1").flush();

        FileTransferACPResp fileTransferACPResp = receiveLineWithTimeout(readers.get("user2"), FileTransferACPResp.class);

        assertEquals(new FileTransferACPResp("ERROR", 10006), fileTransferACPResp);
    }


    @Test
    public void tc81FILETAcceptRequestWrongUserName() throws IOException, NoSuchAlgorithmException {
        setupUser("user1");
        setupUser("user2");

        String requestMessage = Utils.objectToMessage(new FileTransferReq("user2", filePath, Utils.getFileSize(filePath), Utils.createChecksum(String.valueOf(filePath))));
        writers.get("user1").println(requestMessage);
        writers.get("user1").flush();
        writers.get("user2").flush();

        FileTransfer fileTransfer = receiveLineWithTimeout(readers.get("user2"), FileTransfer.class);
        assertEquals(new FileTransfer("user1", file.getName(), Utils.getFileSize(filePath), Utils.createChecksum(String.valueOf(filePath)), fileTransfer.uuid()), fileTransfer);

        String acceptMessage = Utils.objectToMessage(new FileTransferACPReq("usera1", fileTransfer.uuid()));
        writers.get("user2").println(acceptMessage);
        writers.get("user2").flush();
        writers.get("user1").flush();

        FileTransferACPResp fileTransferACPResp = receiveLineWithTimeout(readers.get("user2"), FileTransferACPResp.class);

        assertEquals(new FileTransferACPResp("ERROR", 10007), fileTransferACPResp);
    }

    private <T> T receiveLineWithTimeout(BufferedReader reader, Class<T> expectedClass) throws IOException {
        long timeoutMillis = 3000; // Increase timeout if necessary
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
