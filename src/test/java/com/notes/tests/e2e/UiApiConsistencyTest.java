package com.notes.tests.e2e;

import com.notes.api.AuthApiClient;
import com.notes.api.NotesApiClient;
import com.notes.base.BaseTest;
import com.notes.config.ConfigReader;
import com.notes.pages.LoginPage;
import com.notes.pages.NotesPage;
import com.notes.utils.AllureAttachmentUtil;
import com.notes.utils.RetryAnalyzer;
import com.notes.utils.ScreenshotUtil;
import com.notes.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * ══════════════════════════════════════════════════════════════════════
 * TC-01 — UI ↔ API Data Consistency Check
 * ══════════════════════════════════════════════════════════════════════
 *
 * Objective:
 *   Verify that a note created through the UI is returned by the
 *   GET /notes API with exactly matching Category, Title, and Description.
 *
 * Requirements covered: FR-01, FR-02, FR-03, FR-04, FR-05
 *
 * FIXES applied:
 * ──────────────────────────────────────────────────────────────────────
 * BUG: Step 2 previously created `new NotesPage()` which
 *      instantiated a second NotesPage object and called its own
 *      readiness wait, but the driver was still sitting on the login-
 *      page URL because Step 1's loginAs() result was ignored. This
 *      caused a null-pointer / stale-element cascade.
 *
 * FIX: The result of loginAs() is now captured as `notesPage` and
 *      passed directly into the clickAddNote() call in Step 2.
 *      This ensures the single NotesPage instance is always in sync
 *      with the browser's current state.
 * ══════════════════════════════════════════════════════════════════════
 */
@Epic("Notes Application")
@Feature("Hybrid E2E — UI + API Consistency")
public class UiApiConsistencyTest extends BaseTest {

    private final AuthApiClient  authClient  = new AuthApiClient();
    private final NotesApiClient notesClient = new NotesApiClient();

    @Test(description = "TC-01: UI-created note matches API response (Category, Title, Description)",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("FR-05: UI-created note must appear in API")
    @Severity(SeverityLevel.BLOCKER)
    @Link(name = "Capstone RTM", url = "https://practice.expandtesting.com/notes/api/api-docs/")
    public void tc01_uiCreatedNoteMatchesApiResponse() {

        // ── Test data ────────────────────────────────────────────────
        final String CATEGORY    = "Work";
        final String TITLE       = TestDataGenerator.uniqueTitle();
        final String DESCRIPTION = TestDataGenerator.uniqueDescription();

        log.info("TC-01 START | category='{}' title='{}' description='{}'",
                CATEGORY, TITLE, DESCRIPTION);

        // ── STEP 1: Login via UI ──────────────────────────────────────
        // FIX: capture the returned NotesPage instead of discarding it.
        final NotesPage[] notesPageHolder = new NotesPage[1];

        Allure.step("Step 1: Login via UI and land on dashboard", () -> {
            notesPageHolder[0] = new LoginPage().loginAs(
                    ConfigReader.getTestEmail(),
                    ConfigReader.getTestPassword());
            log.info("FR-01 ✓ — Logged in. Current URL: {}", driver().getCurrentUrl());
        });

        // ── STEP 2: Create note via UI form ───────────────────────────
        // FIX: use the captured notesPage (not a freshly instantiated one)
        Allure.step("Step 2: Create note via UI form", () -> {
            notesPageHolder[0] = notesPageHolder[0]
                    .clickAddNote()
                    .createNote(CATEGORY, TITLE, DESCRIPTION);
            log.info("FR-02 ✓ — Note creation form submitted.");
        });

        NotesPage notesPage = notesPageHolder[0];

        // ── STEP 3: Verify note appears in UI list (FR-03) ────────────
        Allure.step("Step 3: Assert note appears in UI list", () -> {
            Assert.assertTrue(notesPage.isNotePresent(TITLE),
                    "[FR-03] Note '" + TITLE + "' should be visible in the UI after creation.");
            log.info("FR-03 ✓ — Note '{}' present in UI.", TITLE);

            byte[] screenshot = ScreenshotUtil.capture(driver());
            AllureAttachmentUtil.attachScreenshot("Note visible in UI", screenshot);
        });

        // ── STEP 4: Acquire API auth token ────────────────────────────
        final String[] tokenHolder = new String[1];

        Allure.step("Step 4: Acquire API auth token", () -> {
            tokenHolder[0] = authClient.login(
                    ConfigReader.getTestEmail(),
                    ConfigReader.getTestPassword());
            Assert.assertNotNull(tokenHolder[0], "API token must not be null.");
            log.info("API token acquired.");
        });

        // ── STEP 5: GET /notes ────────────────────────────────────────
        final Response[] apiResponseHolder = new Response[1];

        Allure.step("Step 5: GET /notes from API (FR-04)", () -> {
            Response apiResponse = notesClient.getAllNotes(tokenHolder[0]);
            apiResponse.then().statusCode(200);
            apiResponseHolder[0] = apiResponse;

            AllureAttachmentUtil.attachJson(
                    "GET /notes API Response", apiResponse.asPrettyString());
            log.info("FR-04 ✓ — GET /notes returned 200.");
        });

        // ── STEP 6: Data consistency assertions (FR-05) ───────────────
        Allure.step("Step 6: Assert Category, Title, Description match UI ↔ API", () -> {
            Response apiResponse = apiResponseHolder[0];

            String apiTitle       = notesClient.extractTitleByTitle(apiResponse, TITLE);
            String apiCategory    = notesClient.extractCategoryByTitle(apiResponse, TITLE);
            String apiDescription = notesClient.extractDescriptionByTitle(apiResponse, TITLE);

            log.info("API data → title='{}' category='{}' description='{}'",
                    apiTitle, apiCategory, apiDescription);

            Assert.assertEquals(apiTitle, TITLE,
                    "[FR-05] Title mismatch — UI: '" + TITLE + "' | API: '" + apiTitle + "'");
            Assert.assertEquals(apiCategory, CATEGORY,
                    "[FR-05] Category mismatch — UI: '" + CATEGORY + "' | API: '" + apiCategory + "'");
            Assert.assertEquals(apiDescription, DESCRIPTION,
                    "[FR-05] Description mismatch — UI: '" + DESCRIPTION + "' | API: '" + apiDescription + "'");

            log.info("TC-01 PASSED ✓ — All 3 fields match between UI and API.");
        });
    }
}
