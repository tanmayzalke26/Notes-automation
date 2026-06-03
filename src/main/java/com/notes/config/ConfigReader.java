package com.notes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader — loads config.properties and exposes values.
 * Supports environment-variable overrides for CI/CD.
 */
public class ConfigReader {

    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);
    private static final Properties props = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
                log.info("config.properties loaded successfully.");
            } else {
                log.warn("config.properties not found on classpath — using defaults.");
            }
        } catch (IOException e) {
            log.error("Failed to load config.properties", e);
        }
    }

    private ConfigReader() { /* utility class */ }

    /** Returns property value; env variable takes precedence over file. */
    public static String get(String key) {
        String envValue = System.getenv(key.toUpperCase().replace(".", "_"));
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return props.getProperty(key, "");
    }

    public static String getBaseUrl()      { return get("base.url"); }
    public static String getApiBaseUrl()   { return get("api.base.url"); }
    public static String getBrowser()      { return get("browser"); }
    public static boolean isHeadless()     { return Boolean.parseBoolean(get("headless")); }
    public static String getTestEmail()    { return get("test.email"); }
    public static String getTestPassword() { return get("test.password"); }
    public static int getImplicitWait()    { return Integer.parseInt(get("implicit.wait")); }
    public static int getExplicitWait()    { return Integer.parseInt(get("explicit.wait")); }
    public static long getMaxResponseMs()  { return Long.parseLong(get("api.max.response.ms")); }
}
