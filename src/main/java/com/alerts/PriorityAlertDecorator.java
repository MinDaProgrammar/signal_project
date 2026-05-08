package com.alerts;

/**
 * A decorator that tags an alert with a priority level.
 *
 * <p>Prepends a priority label (e.g. {@code [HIGH]}) to the wrapped alert's
 * condition string, making it easy to distinguish urgent alerts in any output
 * stream or dispatch pipeline.</p>
 *
 * <p>Example:
 * <pre>
 *   Alert base = new BloodPressureAlert("1", "CriticalSystolicPressure", 1000L);
 *   Alert urgent = new PriorityAlertDecorator(base, "HIGH");
 *   // urgent.getCondition() → "[HIGH] CriticalSystolicPressure"
 * </pre>
 * </p>
 */
public class PriorityAlertDecorator extends AlertDecorator {

    private final String priority;

    /**
     * Constructs a priority-tagged decorator around the given alert.
     *
     * @param decoratedAlert the alert to wrap
     * @param priority       the priority label to prepend (e.g. {@code "HIGH"}, {@code "CRITICAL"})
     */
    public PriorityAlertDecorator(Alert decoratedAlert, String priority) {
        super(decoratedAlert);
        this.priority = priority;
    }

    /**
     * Returns the priority level assigned to this alert.
     *
     * @return priority string (e.g. {@code "HIGH"})
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Returns the condition string prefixed with the priority tag.
     *
     * @return e.g. {@code "[HIGH] CriticalSystolicPressure"}
     */
    @Override
    public String getCondition() {
        return "[" + priority + "] " + decoratedAlert.getCondition();
    }
}
