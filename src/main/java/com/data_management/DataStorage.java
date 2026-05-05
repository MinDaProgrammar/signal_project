package com.data_management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alerts.AlertGenerator;

/**
 * Manages storage and retrieval of patient data within a healthcare monitoring
 * system. Implemented as a Singleton to ensure only one instance of the data
 * store exists across the entire application.
 */
public class DataStorage {

    private static DataStorage instance;

    private Map<Integer, Patient> patientMap;

    /**
     * Private constructor — use {@link #getInstance()} to obtain the shared instance.
     */
    private DataStorage() {
        this.patientMap = new HashMap<>();
    }

    /**
     * Returns the single shared instance of DataStorage, creating it if it does
     * not yet exist. Thread-safe via synchronization.
     *
     * @return the singleton DataStorage instance
     */
    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    /**
     * Resets the singleton instance. Intended for use in unit tests only so each
     * test starts with a clean storage state.
     */
    public static void resetInstance() {
        instance = null;
    }

    /**
     * Adds or updates patient data in the storage.
     * If the patient does not exist, a new Patient object is created and added to
     * the storage. Otherwise, the new data is added to the existing patient's records.
     *
     * @param patientId        the unique identifier of the patient
     * @param measurementValue the value of the health metric being recorded
     * @param recordType       the type of record, e.g., "HeartRate", "BloodPressure"
     * @param timestamp        the time at which the measurement was taken, in
     *                         milliseconds since the Unix epoch
     */
    public void addPatientData(int patientId, double measurementValue, String recordType, long timestamp) {
        Patient patient = patientMap.get(patientId);
        if (patient == null) {
            patient = new Patient(patientId);
            patientMap.put(patientId, patient);
        }
        patient.addRecord(measurementValue, recordType, timestamp);
    }

    /**
     * Retrieves a list of PatientRecord objects for a specific patient, filtered by
     * a time range.
     *
     * @param patientId the unique identifier of the patient
     * @param startTime the start of the time range, in milliseconds since the Unix epoch
     * @param endTime   the end of the time range, in milliseconds since the Unix epoch
     * @return a list of PatientRecord objects within the specified time range
     */
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getRecords(startTime, endTime);
        }
        return new ArrayList<>();
    }

    /**
     * Retrieves all patients currently stored.
     *
     * @return a list of all patients
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }

    /**
     * Entry point for running the data storage system standalone.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        DataStorage storage = DataStorage.getInstance();

        List<PatientRecord> records = storage.getRecords(1, 1700000000000L, 1800000000000L);
        for (PatientRecord record : records) {
            System.out.println("Record for Patient ID: " + record.getPatientId() +
                    ", Type: " + record.getRecordType() +
                    ", Data: " + record.getMeasurementValue() +
                    ", Timestamp: " + record.getTimestamp());
        }

        AlertGenerator alertGenerator = new AlertGenerator(storage);
        for (Patient patient : storage.getAllPatients()) {
            alertGenerator.evaluateData(patient);
        }
    }
}
