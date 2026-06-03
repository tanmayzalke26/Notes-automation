package com.notes.tests.api;

import com.notes.api.AuthApiClient;
import com.notes.api.NotesApiClient;
import com.notes.config.ConfigReader;
import com.notes.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NotesApiTest — API-layer tests covering FR-04, FR-06, FR-08, FR-09.
 * No browser is used in this class; it is a pure API test.
 */
@Epic("Notes Application")
@Feature("Notes API")
public class NotesApiTest {

    private static final Logger log = LoggerFactory.getLogger(NotesApiTest.class);

    private final AuthApiClient authClient  = new AuthApiClient();
    private final NotesApiClient notesClient = new NotesApiClient();

    private String authToken;
    private String createdNoteId;

    @BeforeClass
    public void authenticate() {
        authToken = authClient.login(
                ConfigReader.getTestEmail(),
                ConfigReader.getTestPassword());
        Assert.assertNotNull(authToken, "Auth token must not be null after login.");
        log.info("API test class authenticated.");
    }

    @Test(description = "TC-API-01: GET /notes returns 200 and a list within 2 seconds",
          priority = 1)
    @Story("FR-04: API GET /notes returns list | FR-08: API response < 2s")
    @Severity(SeverityLevel.CRITICAL)
    public void getNotesReturns200WithinThreshold() {
        Response response = notesClient.getAllNotes(authToken);
        response.then().statusCode(200);
        // Response-time assertion is already inside getAllNotes via ApiBase
        log.info("TC-API-01 PASSED — GET /notes returned 200 within threshold.");
    }

    @Test(description = "TC-API-02: POST /notes creates a note successfully",
          priority = 2)
    @Story("FR-02: Create note")
    @Severity(SeverityLevel.CRITICAL)
    public void createNoteViaApiReturns200() {
        String title       = TestDataGenerator.uniqueTitle();
        String description = TestDataGenerator.uniqueDescription();

        Response response = notesClient.createNote(authToken, "Personal", title, description);
        response.then().statusCode(200);

        createdNoteId = notesClient.extractNoteId(response);
        Assert.assertNotNull(createdNoteId, "Created note ID must not be null.");
        log.info("TC-API-02 PASSED — note created with id: {}", createdNoteId);
    }

    @Test(description = "TC-API-03: DELETE /notes/{id} removes the note (FR-06)",
          priority = 3,
          dependsOnMethods = "createNoteViaApiReturns200")
    @Story("FR-06: Delete note via API")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteNoteViaApiReturns200() {
        Response response = notesClient.deleteNote(authToken, createdNoteId);
        response.then().statusCode(200);
        log.info("TC-API-03 PASSED — note {} deleted.", createdNoteId);
    }

    @Test(description = "TC-API-NEG-01: GET /notes without token returns 401",
          priority = 4)
    @Story("FR-09: Negative scenarios (API)")
    @Severity(SeverityLevel.NORMAL)
    public void getNotesWithoutTokenReturns401() {
        Response response = notesClient.getAllNotes("invalid_token_xyz");
        Assert.assertEquals(response.getStatusCode(), 401,
                "Unauthenticated request should return HTTP 401.");
        log.info("TC-API-NEG-01 PASSED — 401 returned for invalid token.");
    }

    @Test(description = "TC-API-NEG-02: DELETE non-existent note returns 404",
          priority = 5)
    @Story("FR-09: Negative scenarios (API)")
    @Severity(SeverityLevel.NORMAL)
    public void deleteNonExistentNoteReturns404() {
        Response response = notesClient.deleteNote(authToken, "000000000000000000000000");
        Assert.assertEquals(response.getStatusCode(), 404,
                "Deleting a non-existent note should return HTTP 404.");
        log.info("TC-API-NEG-02 PASSED — 404 returned for unknown note id.");
    }
}
