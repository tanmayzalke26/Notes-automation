package com.notes.api;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * NotesApiClient — wraps all /notes CRUD endpoints.
 *
 * Every method:
 *  1. Builds the request with the auth token.
 *  2. Asserts response time &lt; configured threshold.
 *  3. Returns the raw Response so tests can assert status codes and body.
 */
public class NotesApiClient extends ApiBase {

    private static final Logger log = LoggerFactory.getLogger(NotesApiClient.class);

    private static final String NOTES_BASE = "/notes";

    // ──────────────────────────────────────────
    // CRUD Methods
    // ──────────────────────────────────────────

    /**
     * GET /notes — retrieve all notes for the authenticated user.
     */
    public Response getAllNotes(String token) {
        Response response = authenticatedRequest(token)
                .get(NOTES_BASE);

        assertResponseTime(response);
        log.info("GET {} → {}", NOTES_BASE, response.getStatusCode());
        return response;
    }

    /**
     * GET /notes/{id} — retrieve a single note by ID.
     */
    public Response getNoteById(String token, String noteId) {
        String url = NOTES_BASE + "/" + noteId;
        Response response = authenticatedRequest(token)
                .get(url);

        assertResponseTime(response);
        log.info("GET {} → {}", url, response.getStatusCode());
        return response;
    }

    /**
     * POST /notes — create a new note.
     *
     * @param category  "Home" | "Work" | "Personal"
     * @param title     note title
     * @param description note body
     */
    public Response createNote(String token, String category, String title, String description) {
        Map<String, String> body = new HashMap<>();
        body.put("category", category);
        body.put("title", title);
        body.put("description", description);

        Response response = authenticatedRequest(token)
                .body(body)
                .post(NOTES_BASE);

        assertResponseTime(response);
        log.info("POST {} → {} | title='{}'", NOTES_BASE, response.getStatusCode(), title);
        return response;
    }

    /**
     * PATCH /notes/{id} — update an existing note.
     */
    public Response updateNote(String token, String noteId,
                               String category, String title,
                               String description, boolean completed) {
        Map<String, Object> body = new HashMap<>();
        body.put("category", category);
        body.put("title", title);
        body.put("description", description);
        body.put("completed", completed);

        String url = NOTES_BASE + "/" + noteId;
        Response response = authenticatedRequest(token)
                .body(body)
                .patch(url);

        assertResponseTime(response);
        log.info("PATCH {} → {}", url, response.getStatusCode());
        return response;
    }

    /**
     * DELETE /notes/{id} — permanently delete a note.
     */
    public Response deleteNote(String token, String noteId) {
        String url = NOTES_BASE + "/" + noteId;
        Response response = authenticatedRequest(token)
                .delete(url);

        assertResponseTime(response);
        log.info("DELETE {} → {}", url, response.getStatusCode());
        return response;
    }

    // ──────────────────────────────────────────
    // Convenience extractors
    // ──────────────────────────────────────────

    /** Extracts the note ID from a successful create response. */
    public String extractNoteId(Response createResponse) {
        return createResponse.jsonPath().getString("data.id");
    }

    /** Extracts the title field of the note matching the given title (for round-trip verification). */
    public String extractTitleByTitle(Response getAllResponse, String title) {
        return getAllResponse.jsonPath()
                .getString("data.find { it.title == '" + title + "' }.title");
    }

    /** Extracts the category of the first note matching the given title from GET /notes. */
    public String extractCategoryByTitle(Response getAllResponse, String title) {
        return getAllResponse.jsonPath()
                .getString("data.find { it.title == '" + title + "' }.category");
    }

    /** Extracts the description of the first note matching the given title from GET /notes. */
    public String extractDescriptionByTitle(Response getAllResponse, String title) {
        return getAllResponse.jsonPath()
                .getString("data.find { it.title == '" + title + "' }.description");
    }
}
