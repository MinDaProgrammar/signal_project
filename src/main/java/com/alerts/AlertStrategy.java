package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

/**
 * Strategy interface for alert checking
 */
public interface AlertStrategy {

    /**
     * Checks the patient's records for alert conditions
     *
     * @param patient the patient being evaluated
     * @param records all records for that patient
     * @return a list of alerts to trigger, empty if none
     */
    List<Alert> checkAlert(Patient patient, List<PatientRecord> records);
}
