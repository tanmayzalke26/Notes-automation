package com.notes.tests.ui;

import com.notes.base.BaseTest;
import com.notes.config.ConfigReader;
import com.notes.pages.LoginPage;
import com.notes.pages.NoteFormPage;
import com.notes.pages.NotesPage;
import com.notes.utils.RetryAnalyzer;
import com.notes.utils.TestDataGenerator;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * CreateNoteTest — UI tests covering FR-02 and FR-03.
 */
@Epic("Notes Application")
@Feature("Note Management - UI")
public class CreateNoteTest extends BaseTest {

    @Test(description = "TC-UI-02: Create a note via UI and verify it appears in the list",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("FR-02: Create note via UI | FR-03: Note appears instantly in UI list")
    @Severity(SeverityLevel.CRITICAL)
    public void createNoteAppearsInList() {
        String title       = TestDataGenerator.uniqueTitle();
        String description = TestDataGenerator.uniqueDescription();
        String category    = "Work";

        // Step 1: Login
        NotesPage notesPage = new LoginPage().loginAs(
                ConfigReader.getTestEmail(),
                ConfigReader.getTestPassword());

        // Step 2: Open add-note form and fill it
        NoteFormPage form = notesPage.clickAddNote();
        notesPage = form.createNote(category, title, description);

        // Step 3: Assert note is visible in the list (FR-03)
        Assert.assertTrue(notesPage.isNotePresent(title),
                "Note with title '" + title + "' should appear in the UI list after creation.");
        log.info("TC-UI-02 PASSED — note '{}' visible in list.", title);
    }

    @Test(description = "TC-UI-03: Create notes in all three categories",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("FR-02: Create note via UI")
    @Severity(SeverityLevel.NORMAL)
    public void createNoteInEachCategory() {
        NotesPage notesPage = new LoginPage().loginAs(
                ConfigReader.getTestEmail(),
                ConfigReader.getTestPassword());

        String[] categories = {"Home", "Work", "Personal"};

        for (String cat : categories) {
            String title = TestDataGenerator.uniqueTitle() + "_" + cat;
            notesPage = notesPage.clickAddNote()
                                 .createNote(cat, title, "Testing category: " + cat);

            Assert.assertTrue(notesPage.isNotePresent(title),
                    "Note in category '" + cat + "' should appear in list.");
            log.info("Created note in category: {}", cat);
        }
        log.info("TC-UI-03 PASSED — notes created in all three categories.");
    }
}
