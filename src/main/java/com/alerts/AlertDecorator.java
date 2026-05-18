package com.alerts;

/**
 * Abstract base decorator for {@link Alert} — this is the Part 4 Decorator Pattern deliverable.
 *
 * Note: a separate decorator hierarchy exists in com.cardio_generator.outputs
 * (OutputDecorator, TimestampValidationDecorator, PriorityOutputDecorator) which decorates
 * OutputStrategy. That hierarchy was added as an independent improvement to the output pipeline
 * and is not the Part 4 submission.
 *
 * Follows the Decorator pattern: wraps an existing {@link Alert} instance and
 * delegates the core identity (patientId, timestamp) to it.
 * Concrete subclasses override {@link #getCondition()} to append or modify the
 * alert description without changing the original alert object.</p>
 *
 * <p>Example usage:
 * <pre>
 *   Alert base = new BloodPressureAlert("1", "CriticalSystolicPressure", 1000L);
 *   Alert prioritised = new PriorityAlertDecorator(base, "HIGH");
 *   Alert repeated    = new RepeatedAlertDecorator(prioritised, 3, 60_000L);
 * </pre>
 * </p>
 */
public abstract class AlertDecorator extends Alert {

    /** The alert being wrapped by this decorator. */
    protected final Alert decoratedAlert;

    /**
     * Constructs a decorator around the given alert, inheriting its patient ID,
     * condition, and timestamp.
     *
     * @param decoratedAlert the alert to wrap; must not be {@code null}
     */
    public AlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert.getPatientId(),
              decoratedAlert.getCondition(),
              decoratedAlert.getTimestamp());
        this.decoratedAlert = decoratedAlert;
    }

    /**
     * Returns the alert being wrapped by this decorator.
     *
     * @return the inner decorated alert
     */
    public Alert getDecoratedAlert() {
        return decoratedAlert;
    }
}
