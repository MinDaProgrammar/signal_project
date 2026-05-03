package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * One kind of fake patient reading ~ Implementations send each value to an {@link OutputStrategy}
 * The simulator calls {@link #generate(int, OutputStrategy)} on a timer.
 */
public interface PatientDataGenerator {

    /**
     * Makes the next value for this patient and passes it to the output.
     *
     * @param patientId which patient (usually 1 or higher here)
     * @param outputStrategy where to write the value; simulator always passes a non-null one
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
