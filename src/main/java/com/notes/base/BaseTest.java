package com.notes.base;

import com.notes.drivers.DriverFactory;
import com.notes.utils.AllureAttachmentUtil;
import com.notes.utils.ScreenshotUtil;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseTest — parent of every TestNG test class.
 *
 * FIX: setUp() no longer navigates to any URL.
 * Each LoginPage constructor handles its own navigation,
 * preventing the double-load that caused the 200-second page-load timeout
 * observed in the UI-Tests surefire report.
 */
public class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DriverFactory.initDriver();
        driver().manage().window().maximize();
        // Do NOT navigate here — each Page Object's constructor handles navigation.
        // The previous driver().get(baseUrl) + LoginPage() constructor navigation
        // caused two sequential page loads, triggering connection-timeout failures
        // under parallel execution.
        log.info("[Thread {}] WebDriver initialised. Test starting.",
                Thread.currentThread().getId());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            log.warn("Test FAILED: {}", result.getName());
            try {
                byte[] screenshot = ScreenshotUtil.capture(driver());
                if (screenshot != null) {
                    AllureAttachmentUtil.attachScreenshot("Failure Screenshot", screenshot);
                }
            } catch (Exception e) {
                log.warn("Could not capture failure screenshot: {}", e.getMessage());
            }
        }
        DriverFactory.quitDriver();
        log.info("[Thread {}] Test teardown complete.", Thread.currentThread().getId());
    }

    protected WebDriver driver() {
        return DriverFactory.getDriver();
    }
}
