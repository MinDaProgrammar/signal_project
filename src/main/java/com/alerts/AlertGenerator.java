package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 *
 * Alert checking is delegated to a set of {@link AlertStrategy} implementations,
 * one per health metric category.
 */
public class AlertGenerator {

    private DataStorage dataStorage;
    private List<Alert> triggeredAlerts;
    private List<AlertStrategy> strategies;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.triggeredAlerts = new ArrayList<>();
        this.strategies = List.of(
                new BloodPressureStrategy(),
                new OxygenSaturationStrategy(),
                new HeartRateStrategy()
        );
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert} method.
     *
     * Each registered {@link AlertStrategy} is run in order. If a strategy
     * throws an unexpected exception it is caught and logged so the remaining
     * strategies can still run
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        if (patient == null) {
            System.err.println("AlertGenerator: received null patient, skipping.");
            return;
        }

        List<PatientRecord> records = patient.getRecords(0, Long.MAX_VALUE);

        for (AlertStrategy strategy : strategies) {
            try {
                for (Alert alert : strategy.checkAlert(patient, records)) {
                    triggerAlert(alert);
                }
            } catch (Exception e) {
                System.err.println("AlertGenerator: strategy " + strategy.getClass().getSimpleName()
                        + " failed for patient " + patient.getPatientId() + " — " + e.getMessage());
            }
        }
    }

    /**
     * Returns all alerts that were triggered across all {@code evaluateData} calls
     * on this instance
     *
     * @return list of triggered alerts
     */
    public List<Alert> getTriggeredAlerts() {
        return triggeredAlerts;
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        triggeredAlerts.add(alert);
        System.out.println("ALERT [Patient " + alert.getPatientId() + "] "
                + alert.getCondition() + " at " + alert.getTimestamp());
    }
}
