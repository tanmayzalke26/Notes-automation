package com.notes.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

/**
 * TestDataGenerator — provides unique, timestamped test data to avoid collisions
 * during parallel execution or repeated runs.
 */
public class TestDataGenerator {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("MMddHHmmss");
    private static final Random RANDOM = new Random();

    private TestDataGenerator() { /* utility */ }

    public static String uniqueEmail() {
        return "test_" + System.currentTimeMillis() + "@capstone.qa";
    }

    public static String uniqueTitle() {
        return "Note_" + LocalDateTime.now().format(TS);
    }

    public static String uniqueDescription() {
        return "Auto-generated description [" + UUID.randomUUID() + "]";
    }

    public static String uniqueName() {
        return "Tester_" + LocalDateTime.now().format(TS);
    }

    /** Returns a random note category. */
    public static String randomCategory() {
        String[] categories = {"Home", "Work", "Personal"};
        return categories[RANDOM.nextInt(categories.length)];
    }
}
