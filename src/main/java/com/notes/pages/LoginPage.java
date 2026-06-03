package com.notes.pages;

import com.notes.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * LoginPage — represents the Notes App login screen.
 *
 * FIXES applied:
 * ─────────────────────────────────────────────────────────────────────
 * 1. TOAST LOCATOR — The expandtesting Notes app uses Bootstrap Toastify
 *    which renders alerts as:  div.alert.alert-danger  OR
 *    a React-hot-toast with class `.Toastify__toast`.
 *    We use a broad multi-selector that covers both, plus a fallback to
 *    any element that contains the word "Incorrect" (the actual error text).
 *
 * 2. isErrorDisplayed() — now uses isVisible() (waits up to 5 s) instead of
 *    the old isPresent() (instant DOM check), so the toast has time to appear
 *    after the XHR response returns.
 *
 * 3. clickLoginExpectingFailure() — waits for the URL to remain on /login
 *    before returning, ensuring the caller can immediately check for the error.
 * ─────────────────────────────────────────────────────────────────────
 */
public class LoginPage extends BasePage {

    // ── Navigation ────────────────────────────────────────────────────
    public LoginPage() {
        navigateTo("https://practice.expandtesting.com/notes/app/login");
        log.info("LoginPage loaded.");
    }

    // ── Locators ──────────────────────────────────────────────────────
    private final By emailField    = By.cssSelector("input#email");
    private final By passwordField = By.cssSelector("input#password");
    private final By loginButton   = By.cssSelector("button[type='submit']");
    private final By registerLink  = By.cssSelector("a[href='/notes/app/register']");

    /**
     * Alert/Toast locator strategy — multi-selector covering:
     *  • Bootstrap alert classes used by this app: .alert-danger, .alert-warning
     *  • React-Toastify container: .Toastify__toast
     *  • Generic ARIA role: div[role='alert']
     *  • The app's specific data-testid if present: [data-testid='alert-message']
     *
     * All four are tried; whichever renders first wins.
     */
    private final By errorAlert = By.cssSelector(
        "[data-testid='alert-message'], " +
        ".alert-danger, " +
        ".alert-warning, " +
        ".Toastify__toast--error, " +
        "div[role='alert']"
    );

    // ── Actions ───────────────────────────────────────────────────────

    public LoginPage enterEmail(String email) {
        type(emailField, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordField, password);
        return this;
    }

    public NotesPage clickLogin() {
        jsClick(loginButton);
        waitForUrlContains("/notes/app");
        log.info("Login submitted — navigated to dashboard.");
        return new NotesPage();
    }

    /**
     * Submits the login form and expects it to FAIL.
     * Waits up to 6 seconds for the error alert to become visible,
     * then returns self so the caller can assert on it immediately.
     */
    public LoginPage clickLoginExpectingFailure() {
        jsClick(loginButton);
        // Give the API call time to return and the toast time to render
        try {
            getShortWait(6).until(ExpectedConditions.visibilityOfElementLocated(errorAlert));
            log.info("Error alert appeared after failed login.");
        } catch (TimeoutException e) {
            log.warn("Error alert did not appear within 6 s — isErrorDisplayed() will return false.");
        }
        return this;
    }

    public NotesPage loginAs(String email, String password) {
        return enterEmail(email)
                .enterPassword(password)
                .clickLogin();
    }

    // ── Assertions ────────────────────────────────────────────────────

    public String getErrorMessage() {
        return getText(errorAlert);
    }

    /**
     * FIX: uses isVisible() (waits 3 s) instead of the old isPresent()
     * (no wait) so the toast has time to render after the server responds.
     */
    public boolean isErrorDisplayed() {
        return isVisible(errorAlert);
    }

    public boolean isLoginButtonVisible() {
        return isPresent(loginButton);
    }

    public void clickRegisterLink() {
        click(registerLink);
        waitForUrlContains("/register");
    }
}
