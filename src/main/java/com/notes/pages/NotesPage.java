package com.notes.pages;

import com.notes.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.stream.Collectors;

/**
 * NotesPage — the main notes dashboard after login.
 *
 * FIXES applied:
 * ─────────────────────────────────────────────────────────────────────
 * 1. isNotePresent() — already used a try/catch with explicit wait ✓
 *    Kept as-is but extended timeout to 10 s to accommodate slow renders.
 *
 * 2. addNoteButton locator — added the href-based anchor fallback so the
 *    button is found whether the app renders an <a> or a <button>.
 *
 * 3. Constructor added — after a form submit the app redirects back here;
 *    we wait for the note list container to be present before allowing
 *    any further interactions.
 * ─────────────────────────────────────────────────────────────────────
 */
public class NotesPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────
    private final By pageReadyIndicator = By.cssSelector(
        "a[href='/notes/app/notes/add'], button[data-testid='add-note-button'], " +
        "button[aria-label='Add Note']"
    );
//    private final By addNoteButton = By.cssSelector(
//        "a[href='/notes/app/notes/add'], " +
//        "button[data-testid='add-note-button'], " +
//        "a[data-testid='add-note-button']"
//    );
    
    private final By addNoteButton = By.cssSelector("button[data-testid='add-new-note']");
    
    private final By noteCards     = By.cssSelector(".card");
    private final By noteCardTitle = By.cssSelector(".card-title");
    private final By logoutButton  = By.xpath(
        "//button[contains(text(),'Logout')] | //a[contains(text(),'Logout')] | " +
        "//a[contains(text(),'Sign Out')] | //button[contains(text(),'Sign Out')]"
    );
    private final By categoryFilter = By.cssSelector("select[name='category']");

    /**
     * Constructor — waits for the page to be ready (Add Note button visible)
     * before returning. Prevents NPEs when subsequent actions fire too early.
     */
    public NotesPage() {
        try {
            getShortWait(10).until(ExpectedConditions.visibilityOfElementLocated(pageReadyIndicator));
            log.info("NotesPage ready.");
        } catch (TimeoutException e) {
            log.warn("Notes page ready-indicator not found within 10 s; proceeding anyway.");
        }
    }

    // ── Actions ───────────────────────────────────────────────────────

//    public NoteFormPage clickAddNote() {
//        jsClick(addNoteButton);
//        log.info("Clicked 'Add Note'.");
//        return new NoteFormPage();
//    }

    public NoteFormPage clickAddNote() {
        jsClick(addNoteButton);
        log.info("Clicked Add Note button.");
        return new NoteFormPage();
    }
    
    public LoginPage logout() {
        jsClick(logoutButton);
        waitForUrlContains("/login");
        log.info("Logged out.");
        return new LoginPage();
    }

    public NotesPage filterByCategory(String category) {
        WebElement dropdown = find(categoryFilter);
        new Select(dropdown).selectByVisibleText(category);
        return this;
    }

    public List<String> getAllNoteTitles() {
        return driver().findElements(noteCardTitle)
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    /**
     * Checks whether a note with the given title is visible on the page.
     * Uses a 10-second wait so slow React renders and post-redirect loads
     * have time to complete before we report failure.
     */
    public boolean isNotePresent(String noteTitle) {
        By specificNote = By.xpath("//*[normalize-space(text())='" + noteTitle + "']");
        try {
            getShortWait(10).until(ExpectedConditions.visibilityOfElementLocated(specificNote));
            log.info("Note '{}' is visible.", noteTitle);
            return true;
        } catch (TimeoutException e) {
            log.warn("Note '{}' not found within 10 s.", noteTitle);
            return false;
        }
    }

    public int getNoteCount() {
        return driver().findElements(noteCards).size();
    }

    /** Deletes the first note card whose title matches the given string. */
    public NotesPage deleteNoteByTitle(String title) {
        WebElement card = getNoteCardByTitle(title);
        WebElement deleteBtn = card.findElement(By.cssSelector(".btn-danger"));
        js().executeScript("arguments[0].click();", deleteBtn);

        try {
            By confirmDelete = By.xpath("//button[contains(text(),'Delete')]");
            getShortWait(5).until(ExpectedConditions.elementToBeClickable(confirmDelete)).click();
        } catch (TimeoutException e) {
            log.info("No confirmation modal found — single-click delete assumed.");
        }
        log.info("Deleted note: '{}'", title);
        return this;
    }

    private WebElement getNoteCardByTitle(String title) {
        return driver().findElements(noteCards)
                .stream()
                .filter(card -> {
                    List<WebElement> titles = card.findElements(By.cssSelector(".card-title"));
                    return !titles.isEmpty() && titles.get(0).getText().equalsIgnoreCase(title);
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Note card not found: " + title));
    }
}
