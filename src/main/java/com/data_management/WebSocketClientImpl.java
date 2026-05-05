package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A WebSocket client that implements {@link DataReader} for real-time data ingestion.
 *
 * <p>Connects to a WebSocket server broadcasting patient data in CSV format
 * ({@code patientId,timestamp,label,data}), parses each message, and stores
 * numeric records into {@link DataStorage}. Non-numeric data values (e.g.,
 * alert strings like "triggered") are logged and skipped gracefully.</p>
 *
 * <p>Usage example:
 * <pre>
 *   DataStorage storage = DataStorage.getInstance();
 *   WebSocketClientImpl client = new WebSocketClientImpl(
 *       new URI("ws://localhost:8080"), storage);
 *   client.connectBlocking();   // starts receiving data
 *   // ...
 *   client.closeBlocking();
 * </pre>
 * </p>
 */
public class WebSocketClientImpl extends WebSocketClient implements DataReader {

    private DataStorage dataStorage;

    /**
     * Constructs a WebSocketClientImpl that will store received data in the
     * given {@link DataStorage}.
     *
     * @param serverUri   the URI of the WebSocket server to connect to
     * @param dataStorage the storage instance for received patient data
     */
    public WebSocketClientImpl(URI serverUri, DataStorage dataStorage) {
        super(serverUri);
        this.dataStorage = dataStorage;
    }

    /**
     * Called when the WebSocket connection is successfully opened.
     *
     * @param handshakedata the server handshake data
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("WebSocket connection opened: " + getURI());
    }

    /**
     * Called when a message is received from the server.
     * Parses the CSV-formatted message and stores the record.
     * Malformed or non-numeric messages are logged and skipped.
     *
     * @param message the raw message string from the server
     */
    @Override
    public void onMessage(String message) {
        try {
            parseAndStore(message);
        } catch (IllegalArgumentException e) {
            System.err.println("Skipping malformed WebSocket message: \""
                    + message + "\" — " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error processing message: \""
                    + message + "\" — " + e.getMessage());
        }
    }

    /**
     * Called when the WebSocket connection is closed.
     * Logs the reason; if the server closed the connection remotely, a warning
     * is printed to indicate a potential data stream interruption.
     *
     * @param code   the closure code
     * @param reason a human-readable reason for the closure
     * @param remote {@code true} if the server initiated the closure
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (remote) {
            System.err.println("WebSocket connection lost (server closed): "
                    + reason + " (code " + code + ")");
        } else {
            System.out.println("WebSocket connection closed by client (code " + code + ")");
        }
    }

    /**
     * Called when a WebSocket error occurs. Logs the error message.
     *
     * @param ex the exception that was raised
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }

    /**
     * Connects to the WebSocket server and begins receiving data.
     * Blocks until the connection is established or the attempt fails.
     *
     * <p>This fulfils the {@link DataReader#readData} contract for streaming
     * sources: calling this method starts continuous data ingestion.</p>
     *
     * @param dataStorage the storage to write incoming records into
     * @throws IOException if the connection attempt is interrupted
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        this.dataStorage = dataStorage;
        try {
            connectBlocking();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("WebSocket connection was interrupted", e);
        }
    }

    /**
     * Factory-style helper: creates a new {@link WebSocketClientImpl} connected
     * to the given URI. Satisfies the {@link DataReader#connectToServer} contract.
     *
     * @param serverUri   the WebSocket server URI string (e.g. {@code ws://localhost:8080})
     * @param dataStorage the storage instance for received records
     * @throws IllegalArgumentException if the URI is syntactically invalid
     * @throws Exception                if the connection cannot be established
     */
    @Override
    public void connectToServer(String serverUri, DataStorage dataStorage) throws Exception {
        try {
            WebSocketClientImpl client = new WebSocketClientImpl(
                    new URI(serverUri), dataStorage);
            client.connectBlocking();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid WebSocket URI: " + serverUri, e);
        }
    }

    /**
     * Parses a CSV message in the format {@code patientId,timestamp,label,data}
     * and stores it in {@link #dataStorage}.
     *
     * <p>Non-numeric {@code data} values are silently skipped (e.g. alert labels
     * such as "triggered" that cannot be represented as a double).</p>
     *
     * @param message the raw CSV string received from the server
     * @throws IllegalArgumentException if the message does not have exactly 4 fields
     *                                  or the numeric fields cannot be parsed
     */
    private void parseAndStore(String message) {
        String[] parts = message.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    "Expected 4 comma-separated fields, got " + parts.length);
        }

        int patientId;
        long timestamp;
        try {
            patientId = Integer.parseInt(parts[0].trim());
            timestamp = Long.parseLong(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Non-numeric patientId or timestamp: " + e.getMessage());
        }

        String label = parts[2].trim();
        String dataStr = parts[3].trim();

        // Non-numeric values (e.g. "triggered") are valid messages but cannot
        // be stored as doubles — skip them rather than crash.
        try {
            double value = Double.parseDouble(dataStr);
            dataStorage.addPatientData(patientId, value, label, timestamp);
        } catch (NumberFormatException e) {
            System.out.println("Skipping non-numeric data for patient "
                    + patientId + " [" + label + "]: " + dataStr);
        }
    }
}
