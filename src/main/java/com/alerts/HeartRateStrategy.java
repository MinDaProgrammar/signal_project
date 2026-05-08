package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for ECG anomalies and manually
 * triggered alerts from nurses or patients
 */
public class HeartRateStrategy implements AlertStrategy {

    private final AlertFactory factory = new ECGAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        String id = String.valueOf(patient.getPatientId());

        checkECG(id, filterByType(records, "ECG"), alerts);
        checkTriggered(id, filterByType(records, "Alert"), alerts);

        return alerts;
    }

    // Flags any ECG peak more than 1.5x the sliding window average as abnormal
    private void checkECG(String patientId, List<PatientRecord> ecgRecords, List<Alert> alerts) {
        int windowSize = 10;

        for (int i = windowSize; i < ecgRecords.size(); i++) {
            double sum = 0;
            for (int j = i - windowSize; j < i; j++) {
                sum += ecgRecords.get(j).getMeasurementValue();
            }
            double avg = sum / windowSize;
            double current = ecgRecords.get(i).getMeasurementValue();

            if (avg > 0 && current > avg * 1.5) {
                alerts.add(factory.createAlert(patientId, "AbnormalECGPeak", ecgRecords.get(i).getTimestamp()));
            }
        }
    }

    // 1.0 = button pressed/triggered, 0.0 = resolved
    private void checkTriggered(String patientId, List<PatientRecord> alertRecords, List<Alert> alerts) {
        for (PatientRecord r : alertRecords) {
            if (r.getMeasurementValue() == 1.0) {
                alerts.add(factory.createAlert(patientId, "ManualAlert", r.getTimestamp()));
            }
        }
    }

    private List<PatientRecord> filterByType(List<PatientRecord> records, String type) {
        List<PatientRecord> result = new ArrayList<>();
        for (PatientRecord r : records) {
            if (r.getRecordType().equals(type)) result.add(r);
        }
        return result;
    }
}
