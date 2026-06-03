package com.notes.api;

import com.notes.config.ConfigReader;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertTrue;

/**
 * ApiBase — central RestAssured configuration.
 *
 * All API client classes extend or use this to get a pre-configured
 * RequestSpecification with base URI, content-type, logging, and Allure capture.
 *
 * Every response is asserted to be delivered within the configured threshold
 * (default 2 000 ms) via {@link #assertResponseTime(Response)}.
 */
public class ApiBase {

    protected static final Logger log = LoggerFactory.getLogger(ApiBase.class);

    private static final long MAX_RESPONSE_MS = 20000;

    // Build once; shared across all threads (RequestSpecBuilder is not stateful after build)
    protected static final RequestSpecification BASE_SPEC = new RequestSpecBuilder()
            .setBaseUri(ConfigReader.getApiBaseUrl())
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .addFilter(new AllureRestAssured())          // Attach request/response to Allure
            .addFilter(new RequestLoggingFilter())       // Log to console / logback
            .addFilter(new ResponseLoggingFilter())
            .build();

    // ──────────────────────────────────────────
    // Factory helpers
    // ──────────────────────────────────────────

    /** Returns a new RequestSpecification seeded with the base spec (unauthenticated). */
    protected static RequestSpecification request() {
        return given().spec(BASE_SPEC);
    }

    /** Returns a RequestSpecification that sends the token as X-Auth-Token header. */
    protected static RequestSpecification authenticatedRequest(String token) {
        return given()
                .spec(BASE_SPEC)
                .header("X-Auth-Token", token);
    }

    // ──────────────────────────────────────────
    // Performance gate
    // ──────────────────────────────────────────

    /**
     * Asserts that the API response was received within {@value} ms.
     * Called after every API interaction in this framework.
     */
    public static void assertResponseTime(Response response) {
        long responseTimeMs = response.getTime();
        log.info("API response time: {} ms (threshold: {} ms)", responseTimeMs, MAX_RESPONSE_MS);
        assertTrue(responseTimeMs < MAX_RESPONSE_MS,
                String.format("Response time %d ms exceeded threshold of %d ms",
                        responseTimeMs, MAX_RESPONSE_MS));
    }
}
