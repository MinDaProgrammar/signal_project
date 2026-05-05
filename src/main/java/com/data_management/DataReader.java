package com.data_management;

import java.io.IOException;

public interface DataReader {

    /**
     * Reads data from a static source (e.g., a file directory) and stores it
     * in the data storage. Used for batch/file-based data ingestion.
     *
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error reading the data
     */
    void readData(DataStorage dataStorage) throws IOException;

    /**
     * Connects to a real-time data source and begins
     * continuously receiving and storing data. 
     *
     * @param serverUri   the URI of the WebSocket server to connect to
     * @param dataStorage the storage where incoming data will be stored
     * @throws Exception if the connection cannot be established
     */
    default void connectToServer(String serverUri, DataStorage dataStorage) throws Exception {
        throw new UnsupportedOperationException(
                "This DataReader does not support real-time streaming.");
    }
}
