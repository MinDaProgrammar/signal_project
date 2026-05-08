package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Fake alert on/off state for each patient. Alerts are triggered and later cleared at random.
 * Each line uses label {@code "Alert"} and data {@code "triggered"} or {@code "resolved"}.
 */
public class AlertGenerator implements PatientDataGenerator {

    // Removed extra blank line — Google Style Guide §4.6.1 allows at most one consecutive blank line

    // Changed to static final and renamed to UPPER_SNAKE_CASE — Google Style Guide §5.2.4 requires
    // constant names to use UPPER_SNAKE_CASE, and the field is a class-level constant
    private static final Random RANDOM = new Random();

    // Changed field name from alertStates to camelCase and marked final — the array reference is
    // never reassigned after construction, so final is appropriate (Google Style Guide §4.8.2)
    private final boolean[] alertStates; // false = resolved, true = triggered

    /**
     * Creates an AlertGenerator for the given number of patients.
     * Each patient starts with no active alert.
     *
     * @param patientCount the number of patients to track alert states for
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Randomly turns an alert on or off for this patient and writes that to the output.
     *
     * @param patientId      which patient to update
     * @param outputStrategy where to send the alert text
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        if (alertStates[patientId]) {
            // Used RANDOM instead of random to match the renamed constant above
            if (RANDOM.nextDouble() < 0.9) { // 90% chance to resolve the alert each cycle
                alertStates[patientId] = false;
                outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
            }
        } else {
            double lambda = 0.1; // Average alert rate per period — adjust to tune alert frequency
            // Used Math.expm1 for numerical precision when lambda is small (Google Style Guide §7)
            double p = -Math.expm1(-lambda); // Probability of at least one alert in this period
            // Renamed alertTriggered from a previous single-letter variable for readability
            // Google Style Guide §5.2.7: local variable names should be descriptive
            boolean alertTriggered = RANDOM.nextDouble() < p;

            if (alertTriggered) {
                alertStates[patientId] = true;
                outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
            }
        }
    }
}
