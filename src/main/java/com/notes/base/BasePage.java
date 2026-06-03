package com.notes.base;

import com.notes.drivers.DriverFactory;
import com.notes.utils.ScreenshotUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.notes.config.ConfigReader.getExplicitWait;

/**
 * BasePage — base class for all Page Objects.
 * No PageFactory; all locating is done with fully custom methods.
 */
public abstract class BasePage {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    // ─────────────────────────────────────────────────────────────────
    // Driver / Wait
    // ─────────────────────────────────────────────────────────────────

    protected WebDriver driver() {
        return DriverFactory.getDriver();
    }

    protected WebDriverWait getWait() {
        return new WebDriverWait(driver(), Duration.ofSeconds(getExplicitWait()));
    }

    /** Short wait — used for elements that should appear quickly (e.g. toasts). */
    protected WebDriverWait getShortWait(int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds));
    }

    protected JavascriptExecutor js() {
        return (JavascriptExecutor) driver();
    }

    // ─────────────────────────────────────────────────────────────────
    // Element interactions
    // ─────────────────────────────────────────────────────────────────

    protected WebElement find(By locator) {
        return getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        getWait().until(ExpectedConditions.elementToBeClickable(locator)).click();
        log.debug("Clicked: {}", locator);
    }

    protected void type(By locator, String text) {
        WebElement el = find(locator);
        el.clear();
        el.sendKeys(text);
        log.debug("Typed '{}' into: {}", text, locator);
    }

    protected String getText(By locator) {
        return find(locator).getText();
    }

    protected String getValue(By locator) {
        return find(locator).getAttribute("value");
    }

    protected void jsClick(By locator) {
        WebElement el = find(locator);
        js().executeScript("arguments[0].click();", el);
        log.debug("JS-clicked: {}", locator);
    }

    protected void scrollIntoView(By locator) {
        WebElement el = find(locator);
        js().executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    /**
     * Checks whether an element is currently visible (not just present).
     * Uses a 3-second grace wait so the DOM has time to update after an action.
     * This replaces the original non-waiting isPresent() which caused false-negatives.
     */
    protected boolean isVisible(By locator) {
        try {
            getShortWait(3).until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Non-waiting DOM presence check — use only when you are certain the element
     * is already rendered (e.g. after waitForNoteToAppear).
     */
    protected boolean isPresent(By locator) {
        return !driver().findElements(locator).isEmpty();
    }

    protected void waitForInvisibility(By locator) {
        getWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void waitForText(By locator, String text) {
        getWait().until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    protected void waitForUrlContains(String fragment) {
        getWait().until(ExpectedConditions.urlContains(fragment));
    }

    // ─────────────────────────────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────────────────────────────

    protected void navigateTo(String url) {
        driver().get(url);
        log.info("Navigated to: {}", url);
    }

    protected byte[] takeScreenshot() {
        return ScreenshotUtil.capture(driver());
    }
}
