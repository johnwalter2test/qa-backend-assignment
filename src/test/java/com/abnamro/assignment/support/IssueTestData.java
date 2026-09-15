package com.abnamro.assignment.support;

import java.time.LocalDate;
import java.util.UUID;

import com.abnamro.assignment.model.CreateIssueRequest;
import com.abnamro.assignment.model.UpdateIssueRequest;

/**
 * Provides reusable and unique test data for GitLab issue tests.
 */
public final class IssueTestData {

	private IssueTestData() {
		// Utility class - prevent instantiation
	}

	public static CreateIssueRequest validIssue() {
		return new CreateIssueRequest(uniqueTitle("CRUD"),
				"Issue created by the ABN AMRO backend QA automation assignment.");
	}

	/**
	 * Returns a simple valid issue request for CRUD tests.
	 *
	 * @return create issue request with a unique title
	 */

	public static CreateIssueRequest issue(String title, String description) {

		return new CreateIssueRequest(title, description);
	}

	/**
	 * Returns an issue request with the supplied title and description.
	 *
	 * @param title       issue title
	 * @param description issue description
	 * @return create issue request
	 */

	public static CreateIssueRequest fullFieldIssue() {

		return CreateIssueRequest.withOptionalFields(uniqueTitle("FULL-FIELDS"), uniqueDescription("FULL-FIELDS"),
				"automation,api,coverage", true, LocalDate.now().plusDays(30).toString(), "issue");
	}

	/**
	 * Returns an issue request populated with all optional fields set.
	 *
	 * @return create issue request with optional fields
	 */

	public static CreateIssueRequest fullFieldIssue(String title, String description, String labels,
			Boolean confidential, String dueDate, String issueType) {

		return CreateIssueRequest.withOptionalFields(title, description, labels, confidential, dueDate, issueType);
	}

	/**
	 * Returns an issue request with the provided optional field values.
	 *
	 * @param title       issue title
	 * @param description issue description
	 * @param labels      comma-separated labels
	 * @param confidential confidential flag
	 * @param dueDate     due date string
	 * @param issueType   issue type
	 * @return create issue request
	 */

	public static String uniqueTitle(String scenario) {
		return "ABN-QA-" + scenario + "-" + UUID.randomUUID();
	}

	/**
	 * Generates a unique title for a test scenario.
	 *
	 * @param scenario short scenario label
	 * @return unique title string
	 */

	public static String uniqueDescription(String scenario) {
		return "Automated test description for " + scenario + " - " + UUID.randomUUID();
	}

	/**
	 * Generates a unique description for a test scenario.
	 *
	 * @param scenario short scenario label
	 * @return unique description string
	 */

	public static CreateIssueRequest issueWithoutTitle() {
		return new CreateIssueRequest(null, uniqueDescription("MISSING-TITLE"));
	}

	/**
	 * Returns an issue request missing the title (used for negative tests).
	 *
	 * @return create issue request with no title
	 */

	public static CreateIssueRequest issueWithEmptyTitle() {
		return new CreateIssueRequest("", uniqueDescription("EMPTY-TITLE"));
	}

	/**
	 * Returns an issue request with an empty title (used for negative tests).
	 *
	 * @return create issue request with empty title
	 */

	public static UpdateIssueRequest emptyUpdate() {
		return new UpdateIssueRequest(null, null, null, null);
	}

	/**
	 * Returns an empty update request (no fields set).
	 *
	 * @return empty update request
	 */

	public static UpdateIssueRequest invalidStateUpdate() {
		return UpdateIssueRequest.state("invalid-state");
	}

	/**
	 * Returns an update request with an invalid state event (used for negative tests).
	 *
	 * @return update request with invalid state
	 */

	public static String nonexistentProjectPath() {
		return "abn-qa-nonexistent-" + UUID.randomUUID();
	}

	/**
	 * Returns a non-existent project path string for negative tests.
	 *
	 * @return project path string
	 */

	public static CreateIssueRequest unicodeIssue() {
		return new CreateIssueRequest(uniqueTitle("UNICODE") + " 日本語 🚀 café & <test>",
				"Unicode description: தமிழ் 日本語 é ñ 🚀");
	}

	/**
	 * Returns an issue request containing Unicode characters to validate encoding.
	 *
	 * @return create issue request with unicode content
	 */

	public static CreateIssueRequest multilineIssue() {
		return new CreateIssueRequest(uniqueTitle("MULTILINE"), "Line 1\nLine 2\nLine 3\nSpecial chars: !@#$%^&*()");
	}

	/**
	 * Returns an issue request with a multiline description.
	 *
	 * @return create issue request with multiline description
	 */

	public static CreateIssueRequest duplicateTitleIssue(String title) {
		return new CreateIssueRequest(title, uniqueDescription("DUPLICATE"));
	}

	/**
	 * Returns an issue request with a provided title (used to create duplicate-title scenarios).
	 *
	 * @param title duplicate title to use
	 * @return create issue request
	 */
}
