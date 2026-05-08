package com.alerts;

/**
 * An alert triggered by a blood oxygen/saturation anomaly
 */
public class BloodOxygenAlert extends Alert {

    public BloodOxygenAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}
