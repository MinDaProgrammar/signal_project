package com.alerts;

/**
 * Creates {@link ECGAlert} instances for ECG and heart-rate related conditions
 */
public class ECGAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new ECGAlert(patientId, condition, timestamp);
    }
}
