package com.abnamro.assignment.assertions;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.OffsetDateTime;

import com.abnamro.assignment.config.ApiConfig;
import com.abnamro.assignment.model.CreateIssueRequest;
import com.abnamro.assignment.model.IssueResponse;

import io.restassured.response.Response;

/**
 * Provides reusable assertions for GitLab issue responses.
 */
public final class IssueAssertions {

	private IssueAssertions() {
		// Utility class - prevent instantiation
	}

	/**
	 * Validates the HTTP contract and deserializes a successful issue response.
	 */
	public static IssueResponse assertSuccessfulIssueResponse(Response response, int expectedStatus) {

		response.then().statusCode(expectedStatus).contentType("application/json")
				.body(matchesJsonSchemaInClasspath("schemas/issue-response-schema.json"));

		return response.as(IssueResponse.class);
	}

	/**
	 * Validates an issue returned after creation.
	 */
	public static void assertCreatedIssue(IssueResponse actual, CreateIssueRequest expected) {

		assertAll("Created issue validation",

				() -> assertTrue(actual.id() > 0, "Global issue ID must be positive"),

				() -> assertTrue(actual.iid() > 0, "Project issue IID must be positive"),

				() -> assertEquals(ApiConfig.projectId(), actual.projectId(),
						"Issue should belong to configured project"),

				() -> assertEquals(expected.title(), actual.title(), "Issue title should match"),

				() -> assertEquals(expected.description(), actual.description(), "Issue description should match"),

				() -> assertEquals("opened", actual.state(), "New issue should be opened"),

				() -> assertNotNull(actual.createdAt(), "created_at should be present"),

				() -> assertNotNull(actual.updatedAt(), "updated_at should be present"),

				() -> assertFalse(
						OffsetDateTime.parse(actual.updatedAt()).isBefore(OffsetDateTime.parse(actual.createdAt())),
						"updated_at must not be before created_at"),

				() -> assertValidIssueUrl(actual.webUrl(), actual.iid()));
	}

	/**
	 * Ensures an issue still represents the same GitLab resource after retrieval or
	 * update.
	 */
	public static void assertSameIdentity(IssueResponse actual, IssueResponse original) {

		assertAll("Issue identity validation",

				() -> assertEquals(original.id(), actual.id(), "Global ID must remain unchanged"),

				() -> assertEquals(original.iid(), actual.iid(), "IID must remain unchanged"),

				() -> assertEquals(original.projectId(), actual.projectId(), "Project ID must remain unchanged"),

				() -> assertEquals(original.createdAt(), actual.createdAt(),
						"Creation timestamp must remain unchanged"),

				() -> assertFalse(
						OffsetDateTime.parse(actual.updatedAt()).isBefore(OffsetDateTime.parse(original.updatedAt())),
						"updated_at must not move backwards"));
	}

	/**
	 * Validates the current lifecycle state of an issue.
	 */
	public static void assertState(IssueResponse issue, String expectedState) {

		assertEquals(expectedState, issue.state(), "Issue state should match");
	}

	/**
	 * Validates that GitLab returned a meaningful issue URL.
	 */
	private static void assertValidIssueUrl(String webUrl, long iid) {

		assertNotNull(webUrl, "web_url should be present");

		URI url = URI.create(webUrl);

		assertAll("Issue URL validation",

				() -> assertTrue(url.isAbsolute(), "Issue URL should be absolute"),

				() -> assertNotNull(url.getHost(), "Issue URL should contain a host"),

				() -> assertEquals(URI.create(ApiConfig.baseUrl()).getHost(), url.getHost(),
						"Issue URL should point to the configured GitLab host"),

				() -> assertTrue(
						url.getPath().endsWith("/issues/" + iid) || url.getPath().endsWith("/work_items/" + iid),
						"Issue URL should identify IID " + iid));
	}
}