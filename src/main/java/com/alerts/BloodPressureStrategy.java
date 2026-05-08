package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for blood pressure anomalies ~ critical thresholds, trends,
 * and the combined hypotensive hypoxemia condition
 */
public class BloodPressureStrategy implements AlertStrategy {

    private final AlertFactory factory = new BloodPressureAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        String id = String.valueOf(patient.getPatientId());

        List<PatientRecord> systolic = filterByType(records, "SystolicPressure");
        List<PatientRecord> diastolic = filterByType(records, "DiastolicPressure");
        List<PatientRecord> saturation = filterByType(records, "Saturation");

        // Critical thresholds
        for (PatientRecord r : systolic) {
            double v = r.getMeasurementValue();
            if (v > 180 || v < 90) {
                alerts.add(factory.createAlert(id, "CriticalSystolicPressure", r.getTimestamp()));
            }
        }

        for (PatientRecord r : diastolic) {
            double v = r.getMeasurementValue();
            if (v > 120 || v < 60) {
                alerts.add(factory.createAlert(id, "CriticalDiastolicPressure", r.getTimestamp()));
            }
        }

        // Trend ~ 3 consecutive readings each changing by >10
        checkTrend(id, systolic, "SystolicPressureTrend", alerts);
        checkTrend(id, diastolic, "DiastolicPressureTrend", alerts);

        // Combined ~ low BP + low saturation at roughly the same time
        checkHypotensiveHypoxemia(id, systolic, saturation, alerts);

        return alerts;
    }

    private void checkTrend(String patientId, List<PatientRecord> records, String condition, List<Alert> alerts) {
        for (int i = 2; i < records.size(); i++) {
            double a = records.get(i - 2).getMeasurementValue();
            double b = records.get(i - 1).getMeasurementValue();
            double c = records.get(i).getMeasurementValue();

            boolean increasing = (b - a > 10) && (c - b > 10);
            boolean decreasing = (a - b > 10) && (b - c > 10);

            if (increasing || decreasing) {
                alerts.add(factory.createAlert(patientId, condition, records.get(i).getTimestamp()));
            }
        }
    }

    private void checkHypotensiveHypoxemia(String patientId, List<PatientRecord> systolic,
                                            List<PatientRecord> saturation, List<Alert> alerts) {
        long window = 60 * 1000L;

        for (PatientRecord bp : systolic) {
            if (bp.getMeasurementValue() >= 90) continue;

            for (PatientRecord sat : saturation) {
                if (sat.getMeasurementValue() >= 92) continue;
                if (Math.abs(bp.getTimestamp() - sat.getTimestamp()) <= window) {
                    alerts.add(factory.createAlert(patientId, "HypotensiveHypoxemia", bp.getTimestamp()));
                    return;
                }
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
