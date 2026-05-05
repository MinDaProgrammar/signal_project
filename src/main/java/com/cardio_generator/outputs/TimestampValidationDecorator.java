package com.cardio_generator.outputs;

/**
 * A decorator that validates timestamps before passing output to the wrapped strategy.
 * Records with a negative timestamp or a timestamp more than one hour in the future
 * are considered invalid and are silently dropped.
 *
 * This prevents corrupted simulator data from reaching the storage or network layer.
 */
public class TimestampValidationDecorator extends OutputDecorator {

    private static final long ONE_HOUR_MS = 60 * 60 * 1000L;

    /**
     * @param wrapped the strategy to delegate valid records to
     */
    public TimestampValidationDecorator(OutputStrategy wrapped) {
        super(wrapped);
    }

    /**
     * Forwards the record to the wrapped strategy only if the timestamp is valid.
     * A valid timestamp is non-negative and no more than one hour ahead of the
     * current system time.
     *
     * @param patientId the patient identifier
     * @param timestamp the record timestamp in milliseconds since epoch
     * @param label     the type of measurement
     * @param data      the measurement value as a string
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (timestamp < 0) {
            System.err.println("Dropped record for patient " + patientId
                    + " — negative timestamp: " + timestamp);
            return;
        }
        if (timestamp > System.currentTimeMillis() + ONE_HOUR_MS) {
            System.err.println("Dropped record for patient " + patientId
                    + " — timestamp too far in the future: " + timestamp);
            return;
        }
        wrapped.output(patientId, timestamp, label, data);
    }
}
