package com.notes.utils;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * AllureAttachmentUtil — convenience wrappers for attaching artefacts to Allure reports.
 */
public class AllureAttachmentUtil {

    private static final Logger log = LoggerFactory.getLogger(AllureAttachmentUtil.class);

    private AllureAttachmentUtil() { /* utility */ }

    /** Attaches a PNG screenshot to the current Allure test step. */
    public static void attachScreenshot(String name, byte[] screenshot) {
        if (screenshot == null) return;
        Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), ".png");
        log.debug("Screenshot attached to Allure: {}", name);
    }

    /** Attaches plain-text content (e.g. API request/response bodies). */
    public static void attachText(String name, String content) {
        if (content == null) return;
        Allure.addAttachment(name, "text/plain",
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), ".txt");
    }

    /** Attaches JSON content with syntax highlighting in the report. */
    public static void attachJson(String name, String json) {
        if (json == null) return;
        Allure.addAttachment(name, "application/json",
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), ".json");
    }

    /** Attaches an HTML snippet. */
    public static void attachHtml(String name, String html) {
        if (html == null) return;
        Allure.addAttachment(name, "text/html",
                new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)), ".html");
    }
}
