package alerts;

import com.alerts.*;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link com.alerts.AlertGenerator}.
 * Covers all alert types: blood pressure (critical + trend), blood saturation,
 * hypotensive hypoxemia, ECG peaks, and manually triggered alerts.
 */
class AlertGeneratorTest {

    private DataStorage storage;
    private AlertGenerator alertGenerator;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        alertGenerator = new AlertGenerator(storage);
    }

    // Blood Pressure

    @Test
    void testSystolicCriticalHigh() {
        Patient patient = new Patient(1);
        patient.addRecord(185, "SystolicPressure", 1000L);

        alertGenerator.evaluateData(patient);

        assertTrue(containsCondition(alertGenerator.getTriggeredAlerts(), "CriticalSystolicPressure"));
    }

    @Test
    void testSystolicCriticalLow() {
        Patient patient = new Patient(1);
        patient.addRecord(85, "SystolicPressure", 1000L);

        alertGenerator.evaluateData(patient);

        assertTrue(containsCondition(alertGenerator.getTriggeredAlerts(), "CriticalSystolicPressure"));
    }

    @Test
    void testDiastolicCriticalHigh() {
        Patient patient = new Patient(1);
        patient.addRecord(125, "DiastolicPressure", 1000L);

        alertGenerator.evaluateData(patient);

        assertTrue(containsCondition(alertGenerator.getTriggeredAlerts(), "CriticalDiastolicPressure"));
    }

    @Test
    void testSystolicIncreasingTrend() {
        Patient patient = new Patient(1);
        patient.addRecord(120, "SystolicPressure", 1000L);
        patient.addRecord(135, "SystolicPressure", 2000L);
        patient.addRecord(150, "SystolicPressure", 3000L);

        alertGenerator.evaluateData(patient);

        assertTrue(containsCondition(alertGenerator.getTriggeredAlerts(), "SystolicPressureTrend"));
    }

    @Test
    void testSystolicDecreasingTrend() {
        Patient patient = new Patient(1);
        patient.addRecord(150, "SystolicPressure", 1000L);
        patient.addRecord(135, "SystolicPressure", 2000L);
        patient.addRecord(120, "SystolicPressure", 3000L);

        alertGenerator.evaluateData(patient);

        assertTrue(containsCondition(alertGenerator.getTriggeredAlerts(), "SystolicPressureTrend"));
    }

    @Test
    void testNoTrendWhenChangeIsSmall() {
        Patient patient = new Patient(1);
        patient.addRecord(120, "SystolicPressure", 1000L);
        patient.addRecord(125, "SystolicPressure", 2000L);
        patient.addRecord(130, "SystolicPressure", 3000L);

        alertGenerator.evaluateData(patient);

        assertFalse(containsCondition(alertGenerator.getTriggeredAlerts(), "SystolicPressureTrend"));
    }

    // Blood Saturation

    @Test
    void testLowSaturationAlert() {
        Patient patient = new Patient(2);
        patient.addRecord(90, "Saturation", 1000L);

        alertGenerator.evaluateData(patient);

        assertTrue(containsCondition(alertGenerator.getTriggeredAlerts(), "LowSaturation"));
    }

    @Test
    void testNoAlertWhenSaturationNormal() {
        Patient patient = new Patient(2);
        patient.addRecord(95, "Saturation", 1000L);

        alertGenerator.evaluateData(patient);

        assertFalse(containsCondition(alertGenerator.getTriggeredAlerts(), "LowSaturation"));
    }

    @Test
    void testRapidSaturationDropWithinWindow() {
        Patient patient = new Patient(2);
        patient.addRecord(98, "Saturation", 1000L);
        patient.addRecord(92, "Saturation", 1000L + 5 * 60 * 1000L); // 5 minutes later

        alertGenerator.evaluateData(patient);

        assertTrue(containsCondition(alertGenerator.getTriggeredAlerts(), "RapidSaturationDrop"));
    }

    @Test
    void testNoRapidDropWhenOutsideWindow() {
        Patient patient = new Patient(2);
        patient.addRecord(98, "Saturation", 1000L);
        patient.addRecord(92, "Saturation", 1000L + 15 * 60 * 1000L); // 15 minutes later

        alertGenerator.evaluateData(patient);

        assertFalse(containsCondition(alertGenerator.getTriggeredAlerts(), "RapidSaturationDrop"));
    }

    // Combined ~ Hypotensive Hypoxemia

    @Test
    void testHypotensiveHypoxemiaTriggered() {
        Patient patient = new Patient(3);
        patient.addRecord(85, "SystolicPressure", 1000L);
        patient.addRecord(89, "Saturation", 1000L);

        alertGenerator.evaluateData(patient);

        assertTrue(containsCondition(alertGenerator.getTriggeredAlerts(), "HypotensiveHypoxemia"));
    }

    @Test
    void testHypotensiveHypoxemiaNotTriggeredWhenOnlyOneLow() {
        Patient patient = new Patient(3);
        patient.addRecord(85, "SystolicPressure", 1000L);
        patient.addRecord(95, "Saturation", 1000L); // saturation is fine

        alertGenerator.evaluateData(patient);

        assertFalse(containsCondition(alertGenerator.getTriggeredAlerts(), "HypotensiveHypoxemia"));
    }

    // ECG

    @Test
    void testAbnormalECGPeak() {
        Patient patient = new Patient(4);
        // 10 normal readings for the window
        for (int i = 0; i < 10; i++) {
            patient.addRecord(1.0, "ECG", 1000L + i * 100);
        }
        // one spike
        patient.addRecord(3.0, "ECG", 2000L);

        alertGenerator.evaluateData(patient);

        assertTrue(containsCondition(alertGenerator.getTriggeredAlerts(), "AbnormalECGPeak"));
    }

    @Test
    void testNoAlertForNormalECG() {
        Patient patient = new Patient(4);
        for (int i = 0; i < 11; i++) {
            patient.addRecord(1.0, "ECG", 1000L + i * 100);
        }

        alertGenerator.evaluateData(patient);

        assertFalse(containsCondition(alertGenerator.getTriggeredAlerts(), "AbnormalECGPeak"));
    }

    // Triggered alert

    @Test
    void testManualAlertTriggered() {
        Patient patient = new Patient(5);
        patient.addRecord(1.0, "Alert", 1000L);

        alertGenerator.evaluateData(patient);

        assertTrue(containsCondition(alertGenerator.getTriggeredAlerts(), "ManualAlert"));
    }

    @Test
    void testManualAlertNotTriggeredWhenResolved() {
        Patient patient = new Patient(5);
        patient.addRecord(0.0, "Alert", 1000L);

        alertGenerator.evaluateData(patient);

        assertFalse(containsCondition(alertGenerator.getTriggeredAlerts(), "ManualAlert"));
    }

    // Robustness/boundary tests

    @Test
    void testNoAlertsForEmptyRecords() {
        Patient patient = new Patient(6);
        alertGenerator.evaluateData(patient);
        assertTrue(alertGenerator.getTriggeredAlerts().isEmpty());
    }

    @Test
    void testNoAlertsForIrrelevantRecordTypes() {
        Patient patient = new Patient(6);
        patient.addRecord(75.0, "HeartRate", 1000L);
        patient.addRecord(36.6, "Temperature", 1000L);

        alertGenerator.evaluateData(patient);

        assertTrue(alertGenerator.getTriggeredAlerts().isEmpty());
    }

    @Test
    void testSystolicExactlyAtLowerThresholdDoesNotAlert() {
        // 90 is the boundary — only <90 should trigger
        Patient patient = new Patient(6);
        patient.addRecord(90, "SystolicPressure", 1000L);

        alertGenerator.evaluateData(patient);

        assertFalse(containsCondition(alertGenerator.getTriggeredAlerts(), "CriticalSystolicPressure"));
    }

    @Test
    void testSaturationExactlyAt92DoesNotAlert() {
        // 92 is the boundary — only <92 should trigger
        Patient patient = new Patient(6);
        patient.addRecord(92, "Saturation", 1000L);

        alertGenerator.evaluateData(patient);

        assertFalse(containsCondition(alertGenerator.getTriggeredAlerts(), "LowSaturation"));
    }

    @Test
    void testBloodPressureAlertIsCorrectSubtype() {
        Patient patient = new Patient(7);
        patient.addRecord(185, "SystolicPressure", 1000L);

        alertGenerator.evaluateData(patient);

        Alert alert = alertGenerator.getTriggeredAlerts().get(0);
        assertInstanceOf(BloodPressureAlert.class, alert);
    }

    @Test
    void testBloodOxygenAlertIsCorrectSubtype() {
        Patient patient = new Patient(7);
        patient.addRecord(88, "Saturation", 1000L);

        alertGenerator.evaluateData(patient);

        Alert alert = alertGenerator.getTriggeredAlerts().get(0);
        assertInstanceOf(BloodOxygenAlert.class, alert);
    }

    @Test
    void testECGAlertIsCorrectSubtype() {
        Patient patient = new Patient(7);
        for (int i = 0; i < 10; i++) {
            patient.addRecord(1.0, "ECG", 1000L + i * 100);
        }
        patient.addRecord(3.0, "ECG", 2000L);

        alertGenerator.evaluateData(patient);

        assertTrue(alertGenerator.getTriggeredAlerts().stream()
                .anyMatch(a -> a instanceof ECGAlert));
    }

    // Helper to check if any of the alerts in the list matches a condition name
    private boolean containsCondition(List<Alert> alerts, String condition) {
        for (Alert a : alerts) {
            if (a.getCondition().equals(condition)) return true;
        }
        return false;
    }
}
