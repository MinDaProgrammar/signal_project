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
 */
public class AlertGenerator {

    private DataStorage dataStorage;
    private List<Alert> triggeredAlerts;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.triggeredAlerts = new ArrayList<>();
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert} method.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        List<PatientRecord> records = patient.getRecords(0, Long.MAX_VALUE);

        checkBloodPressureAlerts(patient, records);
        checkSaturationAlerts(patient, records);
        checkHypotensiveHypoxemiaAlert(patient, records);
        checkECGAlerts(patient, records);
        checkTriggeredAlerts(patient, records);
    }

    // Blood pressure: critical thresholds + trend across 3 consecutive readings
    private void checkBloodPressureAlerts(Patient patient, List<PatientRecord> records) {
        String id = String.valueOf(patient.getPatientId());

        List<PatientRecord> systolic = filterByType(records, "SystolicPressure");
        List<PatientRecord> diastolic = filterByType(records, "DiastolicPressure");

        for (PatientRecord r : systolic) {
            double v = r.getMeasurementValue();
            if (v > 180 || v < 90) {
                triggerAlert(new Alert(id, "CriticalSystolicPressure", r.getTimestamp()));
            }
        }

        for (PatientRecord r : diastolic) {
            double v = r.getMeasurementValue();
            if (v > 120 || v < 60) {
                triggerAlert(new Alert(id, "CriticalDiastolicPressure", r.getTimestamp()));
            }
        }

        checkTrend(id, systolic, "SystolicPressureTrend");
        checkTrend(id, diastolic, "DiastolicPressureTrend");
    }

    // Triggers an alert if 3 consecutive readings all increase or all decrease by >10 each
    private void checkTrend(String patientId, List<PatientRecord> records, String alertType) {
        for (int i = 2; i < records.size(); i++) {
            double a = records.get(i - 2).getMeasurementValue();
            double b = records.get(i - 1).getMeasurementValue();
            double c = records.get(i).getMeasurementValue();

            boolean increasing = (b - a > 10) && (c - b > 10);
            boolean decreasing = (a - b > 10) && (b - c > 10);

            if (increasing || decreasing) {
                triggerAlert(new Alert(patientId, alertType, records.get(i).getTimestamp()));
            }
        }
    }

    // Saturation: low reading (<92%) and rapid drop (>=5% within 10 minutes)
    private void checkSaturationAlerts(Patient patient, List<PatientRecord> records) {
        String id = String.valueOf(patient.getPatientId());
        List<PatientRecord> satRecords = filterByType(records, "Saturation");
        long tenMinutes = 10 * 60 * 1000L;

        for (PatientRecord r : satRecords) {
            if (r.getMeasurementValue() < 92) {
                triggerAlert(new Alert(id, "LowSaturation", r.getTimestamp()));
            }
        }

        for (int i = 1; i < satRecords.size(); i++) {
            PatientRecord prev = satRecords.get(i - 1);
            PatientRecord curr = satRecords.get(i);

            boolean withinWindow = curr.getTimestamp() - prev.getTimestamp() <= tenMinutes;
            boolean bigDrop = prev.getMeasurementValue() - curr.getMeasurementValue() >= 5;

            if (withinWindow && bigDrop) {
                triggerAlert(new Alert(id, "RapidSaturationDrop", curr.getTimestamp()));
            }
        }
    }

    // Combined: systolic <90 AND saturation <92 at roughly the same time (within 1 minute)
    private void checkHypotensiveHypoxemiaAlert(Patient patient, List<PatientRecord> records) {
        String id = String.valueOf(patient.getPatientId());
        List<PatientRecord> systolic = filterByType(records, "SystolicPressure");
        List<PatientRecord> saturation = filterByType(records, "Saturation");
        long window = 60 * 1000L;

        for (PatientRecord bp : systolic) {
            if (bp.getMeasurementValue() >= 90) continue;

            for (PatientRecord sat : saturation) {
                if (sat.getMeasurementValue() >= 92) continue;
                if (Math.abs(bp.getTimestamp() - sat.getTimestamp()) <= window) {
                    triggerAlert(new Alert(id, "HypotensiveHypoxemia", bp.getTimestamp()));
                    return;
                }
            }
        }
    }

    // ECG: any peak more than 1.5x the sliding window average is considered abnormal
    private void checkECGAlerts(Patient patient, List<PatientRecord> records) {
        String id = String.valueOf(patient.getPatientId());
        List<PatientRecord> ecgRecords = filterByType(records, "ECG");
        int windowSize = 10;

        for (int i = windowSize; i < ecgRecords.size(); i++) {
            double sum = 0;
            for (int j = i - windowSize; j < i; j++) {
                sum += ecgRecords.get(j).getMeasurementValue();
            }
            double avg = sum / windowSize;
            double current = ecgRecords.get(i).getMeasurementValue();

            if (avg > 0 && current > avg * 1.5) {
                triggerAlert(new Alert(id, "AbnormalECGPeak", ecgRecords.get(i).getTimestamp()));
            }
        }
    }

    // Triggered alert from nurse or patient button — stored as 1.0 (triggered) or 0.0 (resolved)
    private void checkTriggeredAlerts(Patient patient, List<PatientRecord> records) {
        String id = String.valueOf(patient.getPatientId());
        List<PatientRecord> alertRecords = filterByType(records, "Alert");

        for (PatientRecord r : alertRecords) {
            if (r.getMeasurementValue() == 1.0) {
                triggerAlert(new Alert(id, "ManualAlert", r.getTimestamp()));
            }
        }
    }

    // Returns all alerts that were triggered during the last evaluateData call
    public List<Alert> getTriggeredAlerts() {
        return triggeredAlerts;
    }

    private List<PatientRecord> filterByType(List<PatientRecord> records, String type) {
        List<PatientRecord> result = new ArrayList<>();
        for (PatientRecord r : records) {
            if (r.getRecordType().equals(type)) {
                result.add(r);
            }
        }
        return result;
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
