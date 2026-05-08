package com.alerts;

/**
 * Base factory class for creating {@link Alert} objects
 *
 * Subclasses define which specific Alert type gets instantiated
 */
public abstract class AlertFactory {

    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}
