package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Fake alert on/off state for each patient. Alerts are triggered and later cleared at random.
 * Each line uses label {@code "Alert"} and data {@code "triggered"} or {@code "resolved"}.
 */
public class AlertGenerator implements PatientDataGenerator {


    private static final Random random = new Random();

    private final boolean[] alertStates; // false = resolved, true = pressed

    /**
     * @param patientCount how many patients
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Randomly turns an alert on or off for this patient and writes that to the output
     *
     * @param patientId which patient to update
     * @param outputStrategy where to send the alert text
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        if (alertStates[patientId]) {
            if (random.nextDouble() < 0.9) { // 90% chance to resolve
                alertStates[patientId] = false;
                outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
            }
        } else {
            double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
            double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
            boolean alertTriggered = random.nextDouble() < p;

            if (alertTriggered) {
                alertStates[patientId] = true;
                outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
            }
        }
    }
}
