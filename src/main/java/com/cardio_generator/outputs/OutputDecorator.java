package com.cardio_generator.outputs;

/**
 * Abstract base decorator for {@link OutputStrategy}.
 * Wraps an existing strategy and delegates to it, allowing subclasses to add
 * behaviour before or after the delegation without modifying the original class.
 */
public abstract class OutputDecorator implements OutputStrategy {

    protected final OutputStrategy wrapped;

    /**
     * @param wrapped the OutputStrategy to decorate
     */
    public OutputDecorator(OutputStrategy wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Delegates to the wrapped strategy. Subclasses override this to add behaviour.
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        wrapped.output(patientId, timestamp, label, data);
    }
}
