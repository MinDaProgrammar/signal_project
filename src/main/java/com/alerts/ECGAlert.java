package com.alerts;

/**
 * An alert triggered by an abnormal ECG reading
 */
public class ECGAlert extends Alert {

    public ECGAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}
