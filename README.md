# Notes App — Capstone Automation Framework

**Stack:** Java 11 · Selenium 4 · RestAssured 5 · TestNG 7 · Allure 2  
**App Under Test:** https://practice.expandtesting.com/notes/app

---

## Project Structure

```
notes-automation/
├── src/
│   ├── main/java/com/notes/
│   │   ├── base/          # BasePage, BaseTest
│   │   ├── pages/         # POM classes (no PageFactory)
│   │   ├── api/           # ApiBase, AuthApiClient, NotesApiClient
│   │   ├── config/        # ConfigReader
│   │   ├── drivers/       # DriverFactory (ThreadLocal WebDriver)
│   │   └── utils/         # ScreenshotUtil, AllureAttachmentUtil, RetryAnalyzer, TestDataGenerator
│   └── test/
│       ├── java/com/notes/tests/
│       │   ├── ui/        # LoginTest, CreateNoteTest
│       │   ├── api/       # NotesApiTest
│       │   └── e2e/       # UiApiConsistencyTest (TC-01), DeletedNoteDisappearsTest
│       └── resources/
│           ├── config.properties
│           ├── testng.xml
│           ├── allure.properties
│           └── logback-test.xml
├── Jenkinsfile
└── pom.xml
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 11+ |
| Maven | 3.8+ |
| Chrome | Latest stable |
| Allure CLI (optional, for local reports) | 2.x |

---

## Quick Start

### 1. Clone & configure
```bash
git clone <repo-url>
cd notes-automation
```

Edit `src/test/resources/config.properties` with your test account credentials:
```properties
test.email=your@email.com
test.password=YourPassword1!
```

### 2. Run all tests
```bash
mvn clean test
```

### 3. Run headless (CI-friendly)
```bash
mvn clean test -Dheadless=true
```

### 4. Run a specific test group
```bash
# UI only
mvn test -Dgroups=ui

# E2E TC-01 only
mvn test -Dtest=UiApiConsistencyTest
```

### 5. Generate Allure report locally
```bash
mvn allure:serve
```
This opens the interactive Allure report in your browser.

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| `ThreadLocal<WebDriver>` | Each parallel test thread owns its own driver; no shared state |
| No PageFactory | Avoids stale element issues; fully custom `find()` + explicit waits |
| `ApiBase` response-time gate | Every API call asserts `< 2000 ms` per FR-08 |
| `RetryAnalyzer` | Auto-retries flaky tests up to 2 times (agentic self-healing) |
| Allure `@Step` / `@Attachment` | Full traceability in the report (screenshots, API bodies) |

---

## CI/CD

The `Jenkinsfile` declarative pipeline:

1. **Checkout** — pulls source from SCM  
2. **Build** — `mvn compile`  
3. **Run Tests** — parallel TestNG suite  
4. **Collect Artefacts** — screenshots, logs, surefire XML  
5. **Publish Allure Report** — interactive report via Allure Jenkins Plugin  
6. **Publish HTML Report** — TestNG summary via HTML Publisher Plugin  

Required Jenkins plugins: **Allure**, **HTML Publisher**, **Maven Integration**.

---

## Requirements Coverage

| Req ID | Description | Test(s) |
|--------|-------------|---------|
| FR-01 | UI login | `LoginTest#validLoginNavigatesToDashboard` |
| FR-02 | Create note via UI | `CreateNoteTest`, `UiApiConsistencyTest` |
| FR-03 | Note appears in UI instantly | `CreateNoteTest`, `UiApiConsistencyTest` |
| FR-04 | GET /notes returns list | `NotesApiTest`, `UiApiConsistencyTest` |
| FR-05 | UI note matches API | `UiApiConsistencyTest (TC-01)` |
| FR-06 | Delete via API | `NotesApiTest`, `DeletedNoteDisappearsTest` |
| FR-07 | Deleted note gone from UI | `DeletedNoteDisappearsTest` |
| FR-08 | API response < 2s | All API clients via `ApiBase#assertResponseTime` |
| FR-09 | Negative scenarios | `LoginTest#invalidCredentials`, `NotesApiTest#negatives` |
