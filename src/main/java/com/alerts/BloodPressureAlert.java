package com.alerts;

/**
 * An alert triggered by a blood pressure anomaly
 */
public class BloodPressureAlert extends Alert {

    public BloodPressureAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}
