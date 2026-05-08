package alerts;

import com.alerts.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that each AlertFactory produces the correct Alert subtype
 * and that the fields are passed through correctly
 */
class AlertFactoryTest {

    @Test
    void testBloodPressureFactoryCreatesCorrectType() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("1", "CriticalSystolicPressure", 1000L);

        assertInstanceOf(BloodPressureAlert.class, alert);
        assertEquals("1", alert.getPatientId());
        assertEquals("CriticalSystolicPressure", alert.getCondition());
        assertEquals(1000L, alert.getTimestamp());
    }

    @Test
    void testBloodOxygenFactoryCreatesCorrectType() {
        AlertFactory factory = new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("2", "LowSaturation", 2000L);

        assertInstanceOf(BloodOxygenAlert.class, alert);
        assertEquals("2", alert.getPatientId());
        assertEquals("LowSaturation", alert.getCondition());
    }

    @Test
    void testECGFactoryCreatesCorrectType() {
        AlertFactory factory = new ECGAlertFactory();
        Alert alert = factory.createAlert("3", "AbnormalECGPeak", 3000L);

        assertInstanceOf(ECGAlert.class, alert);
        assertEquals("3", alert.getPatientId());
        assertEquals("AbnormalECGPeak", alert.getCondition());
    }
}
