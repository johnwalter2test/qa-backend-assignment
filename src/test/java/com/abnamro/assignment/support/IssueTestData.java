package com.abnamro.assignment.support;

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

	public static CreateIssueRequest issue(String title, String description) {

		return new CreateIssueRequest(title, description);
	}

	public static String uniqueTitle(String scenario) {
		return "ABN-QA-" + scenario + "-" + UUID.randomUUID();
	}

	public static String uniqueDescription(String scenario) {
		return "Automated test description for " + scenario + " - " + UUID.randomUUID();
	}

	public static CreateIssueRequest issueWithoutTitle() {
		return new CreateIssueRequest(null, uniqueDescription("MISSING-TITLE"));
	}

	public static CreateIssueRequest issueWithEmptyTitle() {
		return new CreateIssueRequest("", uniqueDescription("EMPTY-TITLE"));
	}

	public static UpdateIssueRequest emptyUpdate() {
		return new UpdateIssueRequest(null, null, null, null);
	}

	public static UpdateIssueRequest invalidStateUpdate() {
		return UpdateIssueRequest.state("invalid-state");
	}

	public static String nonexistentProjectPath() {
		return "abn-qa-nonexistent-" + UUID.randomUUID();
	}

	public static CreateIssueRequest unicodeIssue() {
		return new CreateIssueRequest(uniqueTitle("UNICODE") + " 日本語 🚀 café & <test>",
				"Unicode description: தமிழ் 日本語 é ñ 🚀");
	}

	public static CreateIssueRequest multilineIssue() {
		return new CreateIssueRequest(uniqueTitle("MULTILINE"), "Line 1\nLine 2\nLine 3\nSpecial chars: !@#$%^&*()");
	}

	public static CreateIssueRequest duplicateTitleIssue(String title) {
		return new CreateIssueRequest(title, uniqueDescription("DUPLICATE"));
	}
}
