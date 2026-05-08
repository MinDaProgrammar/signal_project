package com.alerts;

/**
 * A decorator that marks an alert as one that should be repeated at a fixed interval.
 *
 * <p>Appends a repeat annotation to the wrapped alert's condition string and
 * exposes the repeat count and interval so that an alert dispatcher can
 * re-check and re-raise the alert without needing to re-evaluate the raw data.</p>
 *
 * <p>Example:
 * <pre>
 *   Alert base    = new BloodOxygenAlert("2", "LowSaturation", 2000L);
 *   Alert repeated = new RepeatedAlertDecorator(base, 3, 60_000L);
 *   // repeated.getCondition()       → "LowSaturation [Repeat x3]"
 *   // repeated.getRepeatCount()     → 3
 *   // repeated.getRepeatIntervalMs()→ 60000
 * </pre>
 * </p>
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    private final int repeatCount;
    private final long repeatIntervalMs;

    /**
     * Constructs a repeat-annotated decorator around the given alert.
     *
     * @param decoratedAlert   the alert to wrap
     * @param repeatCount      how many times the alert should be re-raised
     * @param repeatIntervalMs the interval in milliseconds between repetitions
     */
    public RepeatedAlertDecorator(Alert decoratedAlert, int repeatCount, long repeatIntervalMs) {
        super(decoratedAlert);
        this.repeatCount = repeatCount;
        this.repeatIntervalMs = repeatIntervalMs;
    }

    /**
     * Returns how many times this alert should be repeated.
     *
     * @return repeat count
     */
    public int getRepeatCount() {
        return repeatCount;
    }

    /**
     * Returns the interval in milliseconds between repetitions.
     *
     * @return repeat interval in milliseconds
     */
    public long getRepeatIntervalMs() {
        return repeatIntervalMs;
    }

    /**
     * Returns the condition string annotated with the repeat count.
     *
     * @return e.g. {@code "LowSaturation [Repeat x3]"}
     */
    @Override
    public String getCondition() {
        return decoratedAlert.getCondition() + " [Repeat x" + repeatCount + "]";
    }
}
