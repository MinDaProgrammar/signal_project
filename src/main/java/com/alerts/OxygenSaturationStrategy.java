package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for blood oxygen anomalies, specifically
 * low saturation and rapid drops within a 10-minute window
 */
public class OxygenSaturationStrategy implements AlertStrategy {

    private final AlertFactory factory = new BloodOxygenAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        String id = String.valueOf(patient.getPatientId());
        List<PatientRecord> satRecords = filterByType(records, "Saturation");
        long tenMinutes = 10 * 60 * 1000L;

        for (PatientRecord r : satRecords) {
            if (r.getMeasurementValue() < 92) {
                alerts.add(factory.createAlert(id, "LowSaturation", r.getTimestamp()));
            }
        }

        for (int i = 1; i < satRecords.size(); i++) {
            PatientRecord prev = satRecords.get(i - 1);
            PatientRecord curr = satRecords.get(i);

            boolean withinWindow = curr.getTimestamp() - prev.getTimestamp() <= tenMinutes;
            boolean bigDrop = prev.getMeasurementValue() - curr.getMeasurementValue() >= 5;

            if (withinWindow && bigDrop) {
                alerts.add(factory.createAlert(id, "RapidSaturationDrop", curr.getTimestamp()));
            }
        }

        return alerts;
    }

    private List<PatientRecord> filterByType(List<PatientRecord> records, String type) {
        List<PatientRecord> result = new ArrayList<>();
        for (PatientRecord r : records) {
            if (r.getRecordType().equals(type)) result.add(r);
        }
        return result;
    }
}
