package com.notes.api;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthApiClient — wraps /users endpoints (register, login, delete account).
 */
public class AuthApiClient extends ApiBase {

    private static final Logger log = LoggerFactory.getLogger(AuthApiClient.class);

    // ──────────────────────────────────────────
    // Endpoints
    // ──────────────────────────────────────────

    private static final String REGISTER_URL    = "/users/register";
    private static final String LOGIN_URL       = "/users/login";
    private static final String DELETE_USER_URL = "/users/delete-account";

    // ──────────────────────────────────────────
    // Methods
    // ──────────────────────────────────────────

    /**
     * POST /users/register
     *
     * @return full Response (caller asserts status code)
     */
    public Response register(String name, String email, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);

        Response response = request()
                .body(body)
                .post(REGISTER_URL);

        assertResponseTime(response);
        log.info("POST {} → {}", REGISTER_URL, response.getStatusCode());
        return response;
    }

    /**
     * POST /users/login
     *
     * @return authentication token extracted from the response body.
     */
    public String login(String email, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Response response = request()
                .body(body)
                .post(LOGIN_URL);

        assertResponseTime(response);
        response.then().statusCode(200);

        String token = response.jsonPath().getString("data.token");
        log.info("Login successful. Token acquired (first 10 chars): {}...",
                token != null && token.length() > 10 ? token.substring(0, 10) : token);
        return token;
    }

    /**
     * DELETE /users/delete-account
     * Permanently removes the authenticated user's account.
     */
    public Response deleteAccount(String token) {
        Response response = authenticatedRequest(token)
                .delete(DELETE_USER_URL);

        assertResponseTime(response);
        log.info("DELETE {} → {}", DELETE_USER_URL, response.getStatusCode());
        return response;
    }
}
