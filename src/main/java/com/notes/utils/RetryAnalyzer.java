package com.notes.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * RetryAnalyzer — agentic auto-retry mechanism for flaky UI / network steps.
 *
 * Annotate a test method with:
 *   {@code @Test(retryAnalyzer = RetryAnalyzer.class)}
 * or apply globally via a TestNG listener.
 *
 * Max retries is controlled by the MAX_RETRY constant (default: 2).
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(RetryAnalyzer.class);

    /** Maximum number of retry attempts per failing test. */
    private static final int MAX_RETRY = 2;

    /** Per-instance counter; TestNG creates one RetryAnalyzer per test method. */
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            log.warn("Retrying test '{}' — attempt {}/{}",
                    result.getName(), retryCount, MAX_RETRY);
            return true;
        }
        log.error("Test '{}' failed after {} retries. No more retries.",
                result.getName(), MAX_RETRY);
        return false;
    }
}
