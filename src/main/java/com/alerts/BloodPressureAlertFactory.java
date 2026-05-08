package com.alerts;

/**
 * Creates {@link BloodPressureAlert} instances for blood pressure related conditions
 */
public class BloodPressureAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BloodPressureAlert(patientId, condition, timestamp);
    }
}
