package data_management;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient(1);
    }

    @Test
    void testGetRecordsWithinRange() {
        patient.addRecord(100.0, "HeartRate", 1000L);
        patient.addRecord(110.0, "HeartRate", 2000L);
        patient.addRecord(120.0, "HeartRate", 3000L);

        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
        assertEquals(110.0, records.get(1).getMeasurementValue());
    }

    @Test
    void testGetRecordsNoneInRange() {
        patient.addRecord(100.0, "HeartRate", 500L);
        patient.addRecord(110.0, "HeartRate", 5000L);

        List<PatientRecord> records = patient.getRecords(1000L, 3000L);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecordsEmptyPatient() {
        List<PatientRecord> records = patient.getRecords(0L, Long.MAX_VALUE);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecordsBoundaryTimestampsIncluded() {
        patient.addRecord(95.0, "Saturation", 1000L);
        patient.addRecord(96.0, "Saturation", 2000L);

        // Boundary values should be inclusive
        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertEquals(2, records.size());
    }

    @Test
    void testGetRecordsAllOutsideRange() {
        patient.addRecord(80.0, "BloodPressure", 100L);
        patient.addRecord(85.0, "BloodPressure", 200L);

        List<PatientRecord> records = patient.getRecords(1000L, 9000L);
        assertTrue(records.isEmpty());
    }

    @Test
    void testAddRecordCorrectPatientId() {
        patient.addRecord(72.0, "HeartRate", 1000L);
        List<PatientRecord> records = patient.getRecords(0L, Long.MAX_VALUE);
        assertEquals(1, records.get(0).getPatientId());
    }
}
