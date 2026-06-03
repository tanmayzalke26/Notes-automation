package com.notes.tests.ui;

import com.notes.base.BaseTest;
import com.notes.config.ConfigReader;
import com.notes.pages.LoginPage;
import com.notes.pages.NotesPage;
import com.notes.utils.RetryAnalyzer;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * LoginTest — UI tests covering FR-01 (login) and FR-09 (negative login).
 *
 * FIXES applied:
 * ─────────────────────────────────────────────────────────────────────
 * invalidCredentialsShowError:
 *   The old test called isErrorDisplayed() immediately after
 *   clickLoginExpectingFailure() returned. Since the original
 *   clickLoginExpectingFailure() had NO wait, and isPresent() was also
 *   non-waiting, the toast check fired before the XHR response arrived.
 *
 *   FIX: clickLoginExpectingFailure() in LoginPage now waits up to 6 s
 *   for the error toast. isErrorDisplayed() uses isVisible() (3 s wait).
 *   The test no longer needs any extra sleep.
 * ─────────────────────────────────────────────────────────────────────
 */
@Epic("Notes Application")
@Feature("Authentication")
public class LoginTest extends BaseTest {

    @Test(description = "TC-UI-01: Valid login navigates to notes dashboard",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("FR-01: UI login should work")
    @Severity(SeverityLevel.CRITICAL)
    public void validLoginNavigatesToDashboard() {
        LoginPage loginPage = new LoginPage();

        NotesPage notesPage = loginPage.loginAs(
                ConfigReader.getTestEmail(),
                ConfigReader.getTestPassword());

        Assert.assertTrue(driver().getCurrentUrl().contains("/notes/app"),
                "Expected to land on notes dashboard after login.");
        log.info("TC-UI-01 PASSED — user is on dashboard.");
    }

    @Test(description = "TC-UI-NEG-01: Invalid credentials show error message",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("FR-09: Negative scenarios (UI)")
    @Severity(SeverityLevel.NORMAL)
    public void invalidCredentialsShowError() {
        LoginPage loginPage = new LoginPage();

        // clickLoginExpectingFailure() now internally waits for the error toast.
        loginPage.enterEmail("invalid@notreal.com")
                 .enterPassword("WrongPass999!")
                 .clickLoginExpectingFailure();

        // isErrorDisplayed() uses isVisible() which waits up to 3 s — belt-and-braces.
        Assert.assertTrue(loginPage.isErrorDisplayed(),
                "Expected an error message to be displayed for invalid credentials.");
        log.info("TC-UI-NEG-01 PASSED — error message displayed.");
    }

    @Test(description = "TC-UI-NEG-02: Empty form submission shows validation error",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("FR-09: Negative scenarios (UI)")
    @Severity(SeverityLevel.MINOR)
    public void emptyFormShowsValidationError() {
        LoginPage loginPage = new LoginPage();
        loginPage.clickLoginExpectingFailure();

        // Either an error toast appears OR the user stays on the login page
        // (browser HTML5 validation may block the submit without a toast).
        Assert.assertTrue(loginPage.isErrorDisplayed() || loginPage.isLoginButtonVisible(),
                "Expected form validation error or user to remain on login page.");
        log.info("TC-UI-NEG-02 PASSED — form validation handled.");
    }
}
