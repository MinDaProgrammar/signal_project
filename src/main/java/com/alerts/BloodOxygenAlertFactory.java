package com.alerts;

/**
 * Creates {@link BloodOxygenAlert} instances for blood oxygen / saturation conditions.
 */
public class BloodOxygenAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BloodOxygenAlert(patientId, condition, timestamp);
    }
}
