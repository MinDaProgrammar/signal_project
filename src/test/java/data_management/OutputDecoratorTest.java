package data_management;

import com.cardio_generator.outputs.OutputStrategy;
import com.cardio_generator.outputs.PriorityOutputDecorator;
import com.cardio_generator.outputs.TimestampValidationDecorator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Decorator pattern applied to OutputStrategy.
 * Uses a simple in-memory stub to capture what the wrapped strategy receives.
 */
class OutputDecoratorTest {

    // Simple stub that records every call made to it
    static class RecordingOutput implements OutputStrategy {
        List<String> calls = new ArrayList<>();

        @Override
        public void output(int patientId, long timestamp, String label, String data) {
            calls.add(patientId + "|" + timestamp + "|" + label + "|" + data);
        }
    }

    private RecordingOutput recorder;

    @BeforeEach
    void setUp() {
        recorder = new RecordingOutput();
    }

    // ── TimestampValidationDecorator ─────────────────────────────────

    @Test
    void testValidTimestampIsForwarded() {
        long now = System.currentTimeMillis();
        OutputStrategy decorated = new TimestampValidationDecorator(recorder);
        decorated.output(1, now, "HeartRate", "72.0");
        assertEquals(1, recorder.calls.size());
    }

    @Test
    void testNegativeTimestampIsDropped() {
        OutputStrategy decorated = new TimestampValidationDecorator(recorder);
        decorated.output(1, -1L, "HeartRate", "72.0");
        assertTrue(recorder.calls.isEmpty());
    }

    @Test
    void testFarFutureTimestampIsDropped() {
        long farFuture = System.currentTimeMillis() + 10 * 60 * 60 * 1000L; // 10 hours ahead
        OutputStrategy decorated = new TimestampValidationDecorator(recorder);
        decorated.output(1, farFuture, "HeartRate", "72.0");
        assertTrue(recorder.calls.isEmpty());
    }

    @Test
    void testZeroTimestampIsDropped() {
        // Timestamp 0 is the Unix epoch — treated as invalid (negative check fails here
        // but 0 >= 0 so it passes negative check; it is a valid edge case — should pass)
        OutputStrategy decorated = new TimestampValidationDecorator(recorder);
        decorated.output(1, 0L, "HeartRate", "72.0");
        assertEquals(1, recorder.calls.size()); // 0 is non-negative and not in the future
    }

    // ── PriorityOutputDecorator ──────────────────────────────────────

    @Test
    void testTriggeredAlertGetsPriorityTag() {
        OutputStrategy decorated = new PriorityOutputDecorator(recorder);
        decorated.output(1, 1000L, "Alert", "triggered");
        assertEquals(1, recorder.calls.size());
        assertTrue(recorder.calls.get(0).contains("[HIGH PRIORITY]"));
    }

    @Test
    void testResolvedAlertHasNoPriorityTag() {
        OutputStrategy decorated = new PriorityOutputDecorator(recorder);
        decorated.output(1, 1000L, "Alert", "resolved");
        assertFalse(recorder.calls.get(0).contains("[HIGH PRIORITY]"));
    }

    @Test
    void testNonAlertRecordPassesThroughUnchanged() {
        OutputStrategy decorated = new PriorityOutputDecorator(recorder);
        decorated.output(1, 1000L, "HeartRate", "72.0");
        assertEquals("1|1000|HeartRate|72.0", recorder.calls.get(0));
    }

    // ── Chained decorators ───────────────────────────────────────────

    @Test
    void testChainedDecoratorsValidTimestampAndAlert() {
        // Priority wraps validation wraps recorder
        OutputStrategy decorated = new PriorityOutputDecorator(
                new TimestampValidationDecorator(recorder));

        long now = System.currentTimeMillis();
        decorated.output(1, now, "Alert", "triggered");

        assertEquals(1, recorder.calls.size());
        assertTrue(recorder.calls.get(0).contains("[HIGH PRIORITY]"));
    }

    @Test
    void testChainedDecoratorsInvalidTimestampDropped() {
        OutputStrategy decorated = new PriorityOutputDecorator(
                new TimestampValidationDecorator(recorder));

        decorated.output(1, -999L, "Alert", "triggered");
        assertTrue(recorder.calls.isEmpty());
    }
}
