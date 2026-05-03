package com.cardio_generator.outputs;

/**
 * Sends one measurement line for one patient (console, file, TCP, etc.)
 */
public interface OutputStrategy {

    /**
     * Writes or streams one record: who, when, what type, and the value text
     *
     * @param patientId patient id
     * @param timestamp time in milliseconds since 1970-01-01
     * @param label type of reading, e.g. {@code "Alert"} or {@code "Saturation"}
     * @param data the value as text
     */
    void output(int patientId, long timestamp, String label, String data);
}
