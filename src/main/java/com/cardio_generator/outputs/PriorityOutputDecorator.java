package com.cardio_generator.outputs;

/**
 * A decorator that flags alert-type records with a priority prefix before
 * passing them to the wrapped strategy.
 *
 * Records with label "Alert" that contain "triggered" are marked as
 * [HIGH PRIORITY], making them easy to distinguish in any output stream.
 * All other records are passed through unchanged.
 */
public class PriorityOutputDecorator extends OutputDecorator {

    /**
     * @param wrapped the strategy to delegate output to
     */
    public PriorityOutputDecorator(OutputStrategy wrapped) {
        super(wrapped);
    }

    /**
     * Prepends a priority tag to alert records before forwarding to the wrapped strategy.
     *
     * @param patientId the patient identifier
     * @param timestamp the record timestamp in milliseconds since epoch
     * @param label     the type of measurement
     * @param data      the measurement value as a string
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if ("Alert".equalsIgnoreCase(label) && "triggered".equalsIgnoreCase(data)) {
            wrapped.output(patientId, timestamp, "[HIGH PRIORITY] " + label, data);
        } else {
            wrapped.output(patientId, timestamp, label, data);
        }
    }
}
