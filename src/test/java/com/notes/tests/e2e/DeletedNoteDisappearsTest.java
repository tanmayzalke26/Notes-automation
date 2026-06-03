package com.notes.tests.e2e;

import com.notes.api.AuthApiClient;
import com.notes.api.NotesApiClient;
import com.notes.base.BaseTest;
import com.notes.config.ConfigReader;
import com.notes.pages.LoginPage;
import com.notes.pages.NotesPage;
import com.notes.utils.AllureAttachmentUtil;
import com.notes.utils.RetryAnalyzer;
import com.notes.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * DeletedNoteDisappearsTest — covers FR-06 and FR-07.
 *
 * Steps:
 *   1. Create a note via API.
 *   2. Verify it appears in the UI.
 *   3. Delete it via the API.
 *   4. Refresh the UI and assert it is gone (FR-07).
 */
@Epic("Notes Application")
@Feature("Hybrid E2E — Delete Sync")
public class DeletedNoteDisappearsTest extends BaseTest {

    private final AuthApiClient  authClient  = new AuthApiClient();
    private final NotesApiClient notesClient = new NotesApiClient();

    @Test(description = "TC-E2E-02: Note deleted via API disappears from UI (FR-06 + FR-07)",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("FR-06: Delete note via API | FR-07: Deleted note disappears from UI")
    @Severity(SeverityLevel.CRITICAL)
    public void deletedNoteDisappearsFromUI() {

        final String TITLE       = TestDataGenerator.uniqueTitle();
        final String DESCRIPTION = TestDataGenerator.uniqueDescription();
        final String CATEGORY    = "Personal";

        // Step 1: Get API token
        String token = authClient.login(
                ConfigReader.getTestEmail(),
                ConfigReader.getTestPassword());

        // Step 2: Create note via API
        Response createResponse = notesClient.createNote(token, CATEGORY, TITLE, DESCRIPTION);
        createResponse.then().statusCode(200);
        String noteId = notesClient.extractNoteId(createResponse);
        log.info("Note created via API. id={}", noteId);

        // Step 3: Login UI and verify note is present
        NotesPage notesPage = new LoginPage().loginAs(
                ConfigReader.getTestEmail(),
                ConfigReader.getTestPassword());

        driver().navigate().refresh();

        Assert.assertTrue(notesPage.isNotePresent(TITLE),
                "Note should be visible in UI before deletion.");
        AllureAttachmentUtil.attachScreenshot("Note present before API delete",
                com.notes.utils.ScreenshotUtil.capture(driver()));

        // Step 4: Delete note via API
        notesClient.deleteNote(token, noteId).then().statusCode(200);
        log.info("Note deleted via API. id={}", noteId);

        // Step 5: Refresh UI and assert note is gone
        driver().navigate().refresh();
        Assert.assertFalse(notesPage.isNotePresent(TITLE),
                "[FR-07] Note should NOT appear in UI after API deletion.");

        AllureAttachmentUtil.attachScreenshot("Note absent after API delete",
                com.notes.utils.ScreenshotUtil.capture(driver()));
        log.info("TC-E2E-02 PASSED — note '{}' no longer visible in UI after API delete.", TITLE);
    }
}
