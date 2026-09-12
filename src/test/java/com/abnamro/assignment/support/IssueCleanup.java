package com.abnamro.assignment.support;

import java.util.LinkedHashSet;
import java.util.Set;

import com.abnamro.assignment.service.IssueService;

import io.restassured.response.Response;

/**
 * Tracks issues created during a test and removes them during cleanup.
 */
public final class IssueCleanup {

	private final IssueService issueService;
	private final Set<Long> issueIids = new LinkedHashSet<>();

	public IssueCleanup(IssueService issueService) {
		this.issueService = issueService;
	}

	/**
	 * Registers an issue for cleanup.
	 */
	public void track(long issueIid) {
		issueIids.add(issueIid);
	}

	/**
	 * Removes all issues created by the current test.
	 *
	 * HTTP 204 means the issue was successfully deleted. HTTP 404 is also
	 * acceptable because the test may already have deleted the issue explicitly.
	 */
	public void cleanup() {

		for (long issueIid : issueIids) {

			Response response = issueService.deleteIssue(issueIid);

			int statusCode = response.statusCode();

			if (statusCode != 204 && statusCode != 404) {
				throw new IllegalStateException(
						"Failed to clean up issue IID " + issueIid + ". HTTP status: " + statusCode);
			}
		}

		issueIids.clear();
	}
}
