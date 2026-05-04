package data_management;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileDataReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testReadValidFile() throws IOException {
        // Write a sample output file in the simulator's format
        File file = tempDir.resolve("Saturation.txt").toFile();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("Patient ID: 1, Timestamp: 1000, Label: Saturation, Data: 98.0%\n");
            fw.write("Patient ID: 1, Timestamp: 2000, Label: Saturation, Data: 97.0%\n");
            fw.write("Patient ID: 2, Timestamp: 1000, Label: Saturation, Data: 95.0%\n");
        }

        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        List<PatientRecord> p1Records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, p1Records.size());

        List<PatientRecord> p2Records = storage.getRecords(2, 0L, Long.MAX_VALUE);
        assertEquals(1, p2Records.size());
        assertEquals(95.0, p2Records.get(0).getMeasurementValue());
    }

    @Test
    void testSkipsNonNumericData() throws IOException {
        // Alert lines have "triggered" or "resolved" as data — should be skipped
        File file = tempDir.resolve("Alert.txt").toFile();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("Patient ID: 1, Timestamp: 1000, Label: Alert, Data: triggered\n");
            fw.write("Patient ID: 1, Timestamp: 2000, Label: Saturation, Data: 98.0%\n");
        }

        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        // Only the numeric line should be stored
        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals("Saturation", records.get(0).getRecordType());
    }

    @Test
    void testThrowsIOExceptionForMissingDirectory() {
        FileDataReader reader = new FileDataReader("/nonexistent/path/abc");
        assertThrows(IOException.class, () -> reader.readData(new DataStorage()));
    }

    @Test
    void testEmptyDirectoryProducesNoRecords() throws IOException {
        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testSkipsMalformedLines() throws IOException {
        File file = tempDir.resolve("Bad.txt").toFile();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("this is not a valid line\n");
            fw.write("Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 75.0\n");
        }

        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        // Only the valid line should be stored
        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(75.0, records.get(0).getMeasurementValue());
    }
}
