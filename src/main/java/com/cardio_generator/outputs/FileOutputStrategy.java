package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Writes each measurement as one line in a {@code .txt} file. Each {@code label} gets its own file under a root folder.
 * Paths are remembered in a map so the same label always uses the same file.
 */
public class FileOutputStrategy implements OutputStrategy {

    // Changed to final when as the reference is assigned only in the constructor
    private final String baseDirectory;


    private final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * @param baseDirectory is the folder where output files are stored
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Makes sure the folder exists, picks the file for {@code label}, and appends one line
     * On error it prints to stderr and returns without throwing so timers keep running.
     *
     * @param patientId patient id
     * @param timestamp time in ms since epoch
     * @param label used in the file name
     * @param data text written on the line
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the filePath variable
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
            // PrintWriter does not throw IOException for failed writes ~ checkError() reports these failures
            if (out.checkError()) {
                System.err.println("Error writing to file " + filePath + " (checkError)");
            }
        // Catch IOException instead of the previously broad Exception
        } catch (IOException e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}