package com.abnamro.assignment.support;

import java.util.UUID;

import com.abnamro.assignment.model.CreateIssueRequest;

/**
 * Provides reusable and unique test data for GitLab issue tests.
 */
public final class IssueTestData {

	private IssueTestData() {
		// Utility class - prevent instantiation
	}

	public static CreateIssueRequest validIssue() {
		return new CreateIssueRequest(
				uniqueTitle("CRUD"),
				"Issue created by the ABN AMRO backend QA automation assignment.");
	}

	public static String uniqueTitle(String scenario) {
		return "ABN-QA-" + scenario + "-" + UUID.randomUUID();
	}

	public static String uniqueDescription(String scenario) {
		return "Automated test description for "
				+ scenario
				+ " - "
				+ UUID.randomUUID();
	}
}
