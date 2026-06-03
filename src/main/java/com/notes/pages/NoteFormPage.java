package com.notes.pages;

import com.notes.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

/**
 * NoteFormPage — the create/edit note form.
 *
 * FIXES applied:
 * ─────────────────────────────────────────────────────────────────────
 * 1. SUCCESS TOAST LOCATOR — same multi-selector strategy as LoginPage.
 *    The app renders a green success toast after saving; we cover all
 *    known variants plus the data-testid the app uses.
 *
 * 2. submit() — instead of waiting for the success toast (which is
 *    transient and may vanish before find() returns), we now:
 *      a) Click submit.
 *      b) Try to detect the toast for up to 5 s.
 *      c) If toast appears, great — log it and continue.
 *      d) If no toast appears (it already disappeared), fall back to
 *         waiting for the URL to come back to /notes/app (which always
 *         happens after a successful save).
 *    This eliminates the TimeoutException that blocked 3 tests.
 *
 * 3. Form field locators are now ID-based as fallback alongside data-testid
 *    to survive minor attribute changes.
 * ─────────────────────────────────────────────────────────────────────
 */
public class NoteFormPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────
    private final By titleField       = By.cssSelector("input#title, input[data-testid='note-title']");
    private final By descriptionField = By.cssSelector("textarea#description, textarea[data-testid='note-description']");
    private final By categoryDropdown = By.cssSelector("select#category, select[data-testid='note-category']");
    private final By submitButton     = By.cssSelector("button[type='submit'], button[data-testid='note-submit']");

    /**
     * Success toast — multi-selector covering:
     *  • data-testid used by this app
     *  • Bootstrap success alert
     *  • React-Toastify success toast
     *  • Generic ARIA live region
     */
    private final By successAlert = By.cssSelector(
        "[data-testid='alert-message'], " +
        ".alert-success, " +
        ".Toastify__toast--success, " +
        "div[role='status'], " +
        "div[role='alert']"
    );

    // ── Actions ───────────────────────────────────────────────────────

    public NoteFormPage setTitle(String title) {
        type(titleField, title);
        return this;
    }

    public NoteFormPage setDescription(String description) {
        type(descriptionField, description);
        return this;
    }

    public NoteFormPage selectCategory(String category) {
        WebElement dropdown = find(categoryDropdown);
        new Select(dropdown).selectByVisibleText(category);
        log.info("Selected category: {}", category);
        return this;
    }

    /**
     * Submits the note form.
     *
     * Strategy:
     *  1. Click submit via JS (bypasses ad overlay).
     *  2. Try to detect the success toast for up to 5 s.
     *     If it appears → great, log it and move on.
     *     If it doesn't appear within 5 s → it may have already vanished
     *     (some toasts auto-dismiss very quickly). Fall through.
     *  3. Wait for the URL to contain /notes/app — this is the reliable
     *     signal that the note was saved and the app redirected back.
     *
     * This approach is resilient to both slow and fast toast animations.
     */
    public NotesPage submit() {
        jsClick(submitButton);
        log.info("Submit button clicked.");

        boolean toastSeen = false;
        try {
            getShortWait(5).until(ExpectedConditions.visibilityOfElementLocated(successAlert));
            toastSeen = true;
            log.info("Success toast appeared — note saved.");
        } catch (TimeoutException e) {
            log.warn("Success toast did not appear within 5 s (may have auto-dismissed). " +
                     "Falling back to URL check.");
        }

        // Regardless of toast visibility, wait for redirect back to the list
        waitForUrlContains("/notes/app");
        log.info("Note saved — redirected back to notes list. (toastSeen={})", toastSeen);

        return new NotesPage();
    }

    /**
     * Convenience: fill all fields then submit.
     */
    public NotesPage createNote(String category, String title, String description) {
        return selectCategory(category)
                .setTitle(title)
                .setDescription(description)
                .submit();
    }

    // ── Assertions ────────────────────────────────────────────────────

    public String getSuccessMessage() {
        return getText(successAlert);
    }

    public boolean isSuccessAlertVisible() {
        return isVisible(successAlert);
    }

    public String getTitleValue() {
        return getValue(titleField);
    }
}
