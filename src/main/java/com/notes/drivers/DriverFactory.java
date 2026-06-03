package com.notes.drivers;

import com.notes.config.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DriverFactory — manages WebDriver lifecycle using ThreadLocal.
 * Each parallel test thread gets its own isolated driver instance.
 */
public class DriverFactory {

    private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);

    /** Thread-safe WebDriver storage — one driver per test thread. */
    private static final ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();

    private DriverFactory() { /* static utility */ }

    /**
     * Initialises the WebDriver for the current thread.
     * Browser type is read from config; headless mode is CI-friendly.
     */
    public static void initDriver() {
        String browser = ConfigReader.getBrowser().toLowerCase().trim();
        boolean headless = ConfigReader.isHeadless();

        WebDriver driver;
        switch (browser) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions ffOptions = new FirefoxOptions();
                if (headless) ffOptions.addArguments("--headless");
                driver = new FirefoxDriver(ffOptions);
                break;

            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                chromeOptions.addArguments(
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-gpu",
                        "--window-size=1920,1080",
                        "--remote-allow-origins=*"
                );
                driver = new ChromeDriver(chromeOptions);
                break;
        }

        driverThread.set(driver);
        log.info("[Thread {}] {} WebDriver initialised (headless={})",
                Thread.currentThread().getId(), browser, headless);
    }

    /** Returns the WebDriver bound to the current thread. */
    public static WebDriver getDriver() {
        WebDriver driver = driverThread.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "WebDriver has not been initialised for thread "
                            + Thread.currentThread().getId()
                            + ". Call DriverFactory.initDriver() first.");
        }
        return driver;
    }

    /** Quits the driver and removes it from the ThreadLocal to prevent memory leaks. */
    public static void quitDriver() {
        WebDriver driver = driverThread.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("[Thread {}] WebDriver quit successfully.",
                        Thread.currentThread().getId());
            } catch (Exception e) {
                log.warn("[Thread {}] Error while quitting WebDriver: {}",
                        Thread.currentThread().getId(), e.getMessage());
            } finally {
                driverThread.remove();
            }
        }
    }
}
