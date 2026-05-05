package data_management;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataStorageTest {

    @BeforeEach
    void setUp() {
        // Reset the singleton before each test so tests don't share state
        DataStorage.resetInstance();
    }

    @Test
    void testSingletonReturnsSameInstance() {
        DataStorage a = DataStorage.getInstance();
        DataStorage b = DataStorage.getInstance();
        assertSame(a, b);
    }

    @Test
    void testAddAndRetrieveSingleRecord() {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "HeartRate", 1000L);
        List<PatientRecord> records = storage.getRecords(1, 1000L, 1000L);
        assertEquals(1, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
        assertEquals("HeartRate", records.get(0).getRecordType());
    }

    @Test
    void testAddMultipleRecordsSamePatient() {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
        assertEquals(200.0, records.get(1).getMeasurementValue());
    }

    @Test
    void testGetRecordsForNonExistentPatient() {
        List<PatientRecord> records = DataStorage.getInstance().getRecords(99, 0L, Long.MAX_VALUE);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetAllPatients() {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "HeartRate", 1000L);
        storage.addPatientData(2, 98.0, "Saturation", 1001L);
        assertEquals(2, storage.getAllPatients().size());
    }

    @Test
    void testRecordsOutsideTimeRangeNotReturned() {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "HeartRate", 500L);
        storage.addPatientData(1, 110.0, "HeartRate", 1500L);
        storage.addPatientData(1, 120.0, "HeartRate", 2500L);

        List<PatientRecord> records = storage.getRecords(1, 1000L, 2000L);
        assertEquals(1, records.size());
        assertEquals(110.0, records.get(0).getMeasurementValue());
    }
}
