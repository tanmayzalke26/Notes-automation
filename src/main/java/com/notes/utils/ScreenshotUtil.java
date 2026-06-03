package com.notes.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ScreenshotUtil — captures screenshots and saves them to /target/screenshots/.
 * Also returns byte[] for direct Allure attachment.
 */
public class ScreenshotUtil {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotUtil.class);
    private static final String SCREENSHOT_DIR = "target/screenshots";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtil() { /* utility */ }

    /**
     * Captures a screenshot and saves it to disk.
     *
     * @param driver  active WebDriver instance
     * @return screenshot bytes (for Allure), or null on failure
     */
    public static byte[] capture(WebDriver driver) {
        if (driver == null) {
            log.warn("Cannot capture screenshot — driver is null.");
            return null;
        }
        try {
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            saveToDisk(bytes);
            return bytes;
        } catch (Exception e) {
            log.error("Screenshot capture failed: {}", e.getMessage());
            return null;
        }
    }

    private static void saveToDisk(byte[] bytes) {
        try {
            Path dir = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(dir);
            String fileName = "screenshot_" + LocalDateTime.now().format(FORMATTER) + ".png";
            Path filePath = dir.resolve(fileName);
            Files.write(filePath, bytes);
            log.info("Screenshot saved: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save screenshot to disk: {}", e.getMessage());
        }
    }
}
