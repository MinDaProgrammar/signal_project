# Code Coverage Report Notes

The overall coverage came out to 49% for instructions and 61% for branches. The screenshot of the full report is included as `coverage_report.png`.

## What was well tested

The two packages we focused on — `com.alerts` (97%) and `com.data_management` (78%) — have good coverage because these are the parts we actually implemented and wrote unit tests for. Things like alert generation, data storage, file reading, and the WebSocket client all have dedicated test classes.

## What wasn't tested and why

**com.cardio_generator.generators (0%)**
These are the data generator classes that produce random ECG, blood pressure, and saturation values on a timer. We didn't write tests for these because the output is random and time-based, which makes unit testing them unreliable and not very meaningful.

**com.cardio_generator (5%)**
This is mostly `HealthDataSimulator`, which starts up a thread scheduler and reads command-line arguments. It's hard to unit test something that spins up multiple threads and requires ports to be open, so we left this untested.

**com.cardio_generator.outputs (19%)**
Some of the output classes like `WebSocketOutputStrategy` and `TcpOutputStrategy` need a real network connection to work, so we couldn't test them in a normal unit test. The decorator classes we added (`TimestampValidationDecorator`, `PriorityOutputDecorator`) are tested in `OutputDecoratorTest`.
