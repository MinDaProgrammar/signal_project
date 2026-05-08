package alerts;

import com.alerts.*;
import com.cardio_generator.HealthDataSimulator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Decorator pattern applied to {@link Alert}.
 * Covers {@link PriorityAlertDecorator}, {@link RepeatedAlertDecorator},
 * chained decorators, and the {@link HealthDataSimulator} Singleton.
 */
class AlertDecoratorTest {

    // ── PriorityAlertDecorator ───────────────────────────────────────────────

    @Test
    void testPriorityDecoratorPrependsTag() {
        Alert base = new BloodPressureAlert("1", "CriticalSystolicPressure", 1000L);
        Alert prioritised = new PriorityAlertDecorator(base, "HIGH");

        assertEquals("[HIGH] CriticalSystolicPressure", prioritised.getCondition());
    }

    @Test
    void testPriorityDecoratorPreservesPatientIdAndTimestamp() {
        Alert base = new BloodOxygenAlert("2", "LowSaturation", 5000L);
        Alert prioritised = new PriorityAlertDecorator(base, "CRITICAL");

        assertEquals("2", prioritised.getPatientId());
        assertEquals(5000L, prioritised.getTimestamp());
    }

    @Test
    void testPriorityDecoratorExposesLevel() {
        Alert base = new ECGAlert("3", "AbnormalECGPeak", 9000L);
        PriorityAlertDecorator dec = new PriorityAlertDecorator(base, "MEDIUM");

        assertEquals("MEDIUM", dec.getPriority());
    }

    @Test
    void testPriorityDecoratorWrapsCorrectAlert() {
        Alert base = new BloodPressureAlert("1", "SystolicTrend", 1000L);
        PriorityAlertDecorator dec = new PriorityAlertDecorator(base, "HIGH");

        assertSame(base, dec.getDecoratedAlert());
    }

    // ── RepeatedAlertDecorator ───────────────────────────────────────────────

    @Test
    void testRepeatedDecoratorAppendsAnnotation() {
        Alert base = new BloodOxygenAlert("1", "LowSaturation", 2000L);
        Alert repeated = new RepeatedAlertDecorator(base, 3, 60_000L);

        assertEquals("LowSaturation [Repeat x3]", repeated.getCondition());
    }

    @Test
    void testRepeatedDecoratorExposesCountAndInterval() {
        Alert base = new BloodPressureAlert("1", "CriticalDiastolicPressure", 3000L);
        RepeatedAlertDecorator dec = new RepeatedAlertDecorator(base, 5, 30_000L);

        assertEquals(5, dec.getRepeatCount());
        assertEquals(30_000L, dec.getRepeatIntervalMs());
    }

    @Test
    void testRepeatedDecoratorPreservesPatientIdAndTimestamp() {
        Alert base = new ECGAlert("4", "AbnormalECGPeak", 7000L);
        Alert repeated = new RepeatedAlertDecorator(base, 2, 15_000L);

        assertEquals("4", repeated.getPatientId());
        assertEquals(7000L, repeated.getTimestamp());
    }

    // ── Chained decorators ───────────────────────────────────────────────────

    @Test
    void testChainedPriorityThenRepeat() {
        Alert base      = new BloodOxygenAlert("1", "LowSaturation", 1000L);
        Alert priority  = new PriorityAlertDecorator(base, "HIGH");
        Alert repeated  = new RepeatedAlertDecorator(priority, 3, 60_000L);

        assertEquals("[HIGH] LowSaturation [Repeat x3]", repeated.getCondition());
    }

    @Test
    void testChainedRepeatThenPriority() {
        Alert base      = new BloodPressureAlert("1", "CriticalSystolicPressure", 1000L);
        Alert repeated  = new RepeatedAlertDecorator(base, 2, 30_000L);
        Alert priority  = new PriorityAlertDecorator(repeated, "CRITICAL");

        assertEquals("[CRITICAL] CriticalSystolicPressure [Repeat x2]", priority.getCondition());
    }

    // ── HealthDataSimulator Singleton ────────────────────────────────────────

    @Test
    void testHealthDataSimulatorSingletonReturnsSameInstance() {
        HealthDataSimulator a = HealthDataSimulator.getInstance();
        HealthDataSimulator b = HealthDataSimulator.getInstance();
        assertSame(a, b);
    }
}
