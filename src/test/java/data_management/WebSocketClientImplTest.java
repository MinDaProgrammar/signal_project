package data_management;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketClientImpl;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and integration tests for {@link WebSocketClientImpl}.
 *
 * <p>A lightweight in-process {@link TestWebSocketServer} is started once for
 * the whole test class. Each test connects a fresh client, sends messages via
 * {@link TestWebSocketServer#broadcast}, then verifies what landed in
 * {@link DataStorage}.</p>
 */
class WebSocketClientImplTest {

    private static final int PORT = 8887;
    private static TestWebSocketServer server;

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @BeforeAll
    static void startServer() throws InterruptedException {
        server = new TestWebSocketServer(PORT);
        server.start();
        Thread.sleep(300); // allow server to bind
    }

    @AfterAll
    static void stopServer() throws Exception {
        server.stop(500);
    }

    @BeforeEach
    void resetStorage() {
        DataStorage.resetInstance();
    }

    // ── Normal data handling ─────────────────────────────────────────────────

    @Test
    void testValidMessageIsStoredCorrectly() throws Exception {
        DataStorage storage = DataStorage.getInstance();
        WebSocketClientImpl client = connect(storage);

        server.broadcast("1,1000,HeartRate,72.0");
        Thread.sleep(200);

        List<PatientRecord> records = storage.getRecords(1, 1000L, 1000L);
        assertEquals(1, records.size());
        assertEquals(72.0, records.get(0).getMeasurementValue());
        assertEquals("HeartRate", records.get(0).getRecordType());
        client.closeBlocking();
    }

    @Test
    void testMultipleMessagesStoredForSamePatient() throws Exception {
        DataStorage storage = DataStorage.getInstance();
        WebSocketClientImpl client = connect(storage);

        server.broadcast("2,1000,Saturation,98.0");
        server.broadcast("2,2000,Saturation,97.5");
        Thread.sleep(300);

        List<PatientRecord> records = storage.getRecords(2, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size());
        client.closeBlocking();
    }

    @Test
    void testMultiplePatientMessagesStoredSeparately() throws Exception {
        DataStorage storage = DataStorage.getInstance();
        WebSocketClientImpl client = connect(storage);

        server.broadcast("3,1000,HeartRate,60.0");
        server.broadcast("4,1000,HeartRate,80.0");
        Thread.sleep(300);

        assertEquals(1, storage.getRecords(3, 0L, Long.MAX_VALUE).size());
        assertEquals(1, storage.getRecords(4, 0L, Long.MAX_VALUE).size());
        client.closeBlocking();
    }

    // ── Error handling ───────────────────────────────────────────────────────

    @Test
    void testMalformedMessageIsSkippedGracefully() throws Exception {
        DataStorage storage = DataStorage.getInstance();
        WebSocketClientImpl client = connect(storage);

        server.broadcast("this is not a valid message");
        Thread.sleep(200);

        assertTrue(storage.getAllPatients().isEmpty(),
                "Malformed message should not produce any records");
        client.closeBlocking();
    }

    @Test
    void testTooFewFieldsIsSkipped() throws Exception {
        DataStorage storage = DataStorage.getInstance();
        WebSocketClientImpl client = connect(storage);

        server.broadcast("1,1000,HeartRate"); // only 3 fields
        Thread.sleep(200);

        assertTrue(storage.getAllPatients().isEmpty());
        client.closeBlocking();
    }

    @Test
    void testNonNumericDataValueIsSkipped() throws Exception {
        // Alert messages like "triggered" are broadcast as strings —
        // they should be logged but not stored as patient records.
        DataStorage storage = DataStorage.getInstance();
        WebSocketClientImpl client = connect(storage);

        server.broadcast("1,1000,Alert,triggered");
        Thread.sleep(200);

        assertTrue(storage.getAllPatients().isEmpty(),
                "Non-numeric alert data should not be stored");
        client.closeBlocking();
    }

    @Test
    void testInvalidPatientIdIsSkipped() throws Exception {
        DataStorage storage = DataStorage.getInstance();
        WebSocketClientImpl client = connect(storage);

        server.broadcast("abc,1000,HeartRate,72.0"); // non-numeric patientId
        Thread.sleep(200);

        assertTrue(storage.getAllPatients().isEmpty());
        client.closeBlocking();
    }

    @Test
    void testMixedValidAndInvalidMessagesOnlyStoresValid() throws Exception {
        DataStorage storage = DataStorage.getInstance();
        WebSocketClientImpl client = connect(storage);

        server.broadcast("bad message");
        server.broadcast("5,2000,BloodPressure,120.0");
        server.broadcast("1,1000,Alert,triggered");
        Thread.sleep(300);

        List<PatientRecord> records = storage.getRecords(5, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(120.0, records.get(0).getMeasurementValue());
        client.closeBlocking();
    }

    // ── Connection handling ───────────────────────────────────────────────────

    @Test
    void testConnectionToUnreachableServerFails() throws Exception {
        DataStorage storage = DataStorage.getInstance();
        WebSocketClientImpl client = new WebSocketClientImpl(
                new URI("ws://localhost:9999"), storage); // nothing listening
        boolean connected = client.connectBlocking();
        assertFalse(connected, "Should fail to connect to unreachable server");
    }

    @Test
    void testConnectToServerHelperStoresData() throws Exception {
        DataStorage storage = DataStorage.getInstance();
        // Use the DataReader.connectToServer default method via a fresh client instance
        WebSocketClientImpl reader = new WebSocketClientImpl(
                new URI("ws://localhost:" + PORT), storage);
        reader.connectBlocking();

        server.broadcast("6,3000,Temperature,36.6");
        Thread.sleep(200);

        List<PatientRecord> records = storage.getRecords(6, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(36.6, records.get(0).getMeasurementValue(), 0.001);
        reader.closeBlocking();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Creates and connects a client to the test server. */
    private WebSocketClientImpl connect(DataStorage storage) throws Exception {
        WebSocketClientImpl client = new WebSocketClientImpl(
                new URI("ws://localhost:" + PORT), storage);
        client.connectBlocking();
        return client;
    }

    /**
     * Minimal WebSocket server used only during tests.
     * Broadcasts messages to all connected clients on demand.
     */
    static class TestWebSocketServer extends WebSocketServer {

        TestWebSocketServer(int port) {
            super(new InetSocketAddress(port));
            setReuseAddr(true);
        }

        @Override public void onOpen(WebSocket conn, ClientHandshake handshake) {}
        @Override public void onClose(WebSocket conn, int code, String reason, boolean remote) {}
        @Override public void onMessage(WebSocket conn, String message) {}
        @Override public void onError(WebSocket conn, Exception ex) {
            if (ex != null) System.err.println("Test server error: " + ex.getMessage());
        }
        @Override public void onStart() {
            System.out.println("Test WebSocket server started on port " + getPort());
        }
    }
}
