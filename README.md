# GitLab Issues API Test Automation

A Java REST API automation framework for testing the GitLab Issues API against a real project. It provides reusable API abstractions, independent test data and a consolidated HTML execution report.

## Solution Overview

REST Assured handles API calls, JUnit 5 executes tests and assertions, Jackson maps JSON, and Maven manages dependencies and builds. JSON Schema validation checks issue responses. Configuration, service, model, assertions, support/test-data and test layers keep the framework reusable.

Implemented scenarios include issue CRUD, negative requests, search/filtering, pagination, state transitions, Unicode and duplicate-title edge cases, time tracking, and an end-to-end issue lifecycle.

## Project Structure

```text
src/test/java/com/abnamro/assignment/
  config/        API configuration and request specifications
  service/       GitLab API service methods
  model/         Request and response models
  assertions/    Reusable API assertions
  support/       Test data and cleanup utilities
  tests/         API test scenarios

src/test/resources/
  schemas/       JSON response schemas
```

## Prerequisites

- Java compiler target **17**, as configured in `pom.xml`. The verified local setup uses JDK 21 to run Maven.
- Maven (verified with 3.9.9).
- A GitLab project with Issues enabled.
- A GitLab API/access token with `api` scope and project permissions for issue creation, updates, deletion, confidential issues and time tracking.

```bash
java -version
mvn -version
```

Check `mvn -version` for the JDK Maven actually uses.

## Configuration

`ApiConfig` requires `GITLAB_PROJECT_ID` (numeric) and `GITLAB_TOKEN`. The optional `GITLAB_BASE_URL` defaults to `https://gitlab.com/api/v4`.

Configuration precedence is **JVM system properties > local properties > environment variables**. JVM property names match the local property keys below. Run commands from the project root. A `.env` file is not loaded automatically.

### Environment variables

```powershell
$env:GITLAB_PROJECT_ID = "12345678"
$env:GITLAB_TOKEN = "your_gitlab_access_token"

mvn clean test
```

### Local properties

Alternatively, create `gitlab.local.properties` beside `pom.xml`:

```properties
gitlab.base.url=https://gitlab.com/api/v4
gitlab.project.id=12345678
gitlab.token=your_gitlab_access_token
```

Replace the placeholders with your own configuration. The local file is ignored by Git; **never commit real credentials**. Local values override environment variables. Requests use Bearer authentication; token acquisition and refresh are outside this framework.

## Build and Run

```bash
mvn clean test
```

This cleans generated output, compiles the tests and executes the full suite. After successful execution, Maven generates the consolidated HTML report.

## Running Tests

The following class and tags exist in the current suite:

```bash
mvn clean test
mvn clean test -Dtest=IssueCrudTest
mvn clean test "-Dgroups=crud"
mvn clean test "-Dgroups=negative"
mvn clean test "-Dgroups=edge-case"
mvn clean test "-Dgroups=state"
mvn clean test "-Dgroups=time-tracking"
mvn clean test "-Dgroups=e2e"
```

Tags are case-sensitive. Use the unfiltered command for a complete submission run; selected runs report only the selected tests. `clean` prevents old XML results from appearing in a later report.

## Test Reports

Maven Surefire executes the tests and writes XML/text results. Maven Surefire Report Plugin then consolidates those results into HTML without running the tests again.

After a successful `mvn clean test`:

```text
target/reports/consolidated-test-report.html
target/surefire-reports/
```

If tests fail, Maven returns a failure and stops before HTML generation. Run `mvn surefire-report:report-only` without cleaning to render the existing failed results; this does not change their outcome.

### Test Execution Report

[Test Execution Report](reports/latest/consolidated-test-report.html)

A snapshot of the latest successful test execution is included in the repository. The complete report directory is retained so the HTML report can be opened locally with its required resources.

Open the HTML report directly at `reports/latest/consolidated-test-report.html`.

## Assumptions and Trade-offs

- Tests call a real GitLab project; network/API availability and permissions affect execution.

- Unique test data reduces collisions. Created issues are cleaned up where possible; interrupted runs can leave data behind.

- The 101-issue pagination test is intentionally slower. It validates page sizes of 100, 1 and 0; the end-to-end scenario also checks pagination across three issues and verifies their IDs.

- This suite validates implemented functional scenarios, not complete GitLab API coverage. Dedicated authentication/authorization failure tests are not implemented.

## Coverage Summary

| Area | Coverage |
| --- | --- |
| Issue CRUD | Create, retrieve, update and delete |
| Negative scenarios | Missing/empty title, invalid update and deleted resources |
| Search / filtering | Unique search marker and state filtering |
| Pagination | 101-item boundary and multi-page flow |
| State transitions | Close and reopen |
| Edge cases | Unicode and duplicate titles |
| Time tracking | Estimate, spent time and reset |
| End-to-end | Full issue lifecycle |
