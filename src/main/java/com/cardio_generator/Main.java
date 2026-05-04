package com.cardio_generator;

import com.data_management.DataStorage;

import java.io.IOException;

/**
 * Entry point that allows running either the HealthDataSimulator or DataStorage
 * depending on the first command-line argument.
 *
 * Usage:
 *   java -jar cardio_generator.jar DataStorage   → runs DataStorage main
 *   java -jar cardio_generator.jar               → runs HealthDataSimulator (default)
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("DataStorage")) {
            DataStorage.main(new String[]{});
        } else {
            HealthDataSimulator.main(args);
        }
    }
}
