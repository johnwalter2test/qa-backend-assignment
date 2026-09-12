package com.abnamro.assignment.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.abnamro.assignment.assertions.IssueAssertions;
import com.abnamro.assignment.model.CreateIssueRequest;
import com.abnamro.assignment.model.IssueResponse;
import com.abnamro.assignment.model.UpdateIssueRequest;
import com.abnamro.assignment.service.IssueService;
import com.abnamro.assignment.support.IssueCleanup;
import com.abnamro.assignment.support.IssueTestData;

import io.restassured.response.Response;

class IssueStateTest {

	private IssueService issueService;
	private IssueCleanup cleanup;

	@BeforeEach
	void setUp() {
		issueService = new IssueService();
		cleanup = new IssueCleanup(issueService);
	}

	@AfterEach
	void tearDown() {
		cleanup.cleanup();
	}

	@Test
	@DisplayName("Close and reopen an existing issue")
	void shouldCloseAndReopenIssue() {
		IssueResponse createdIssue = createTrackedIssue();

		Response closeResponse = issueService.updateIssue(createdIssue.iid(), UpdateIssueRequest.state("close"));
		IssueResponse closedIssue = IssueAssertions.assertSuccessfulIssueResponse(closeResponse, 200);
		IssueAssertions.assertSameIdentity(closedIssue, createdIssue);
		IssueAssertions.assertState(closedIssue, "closed");

		Response closedGetResponse = issueService.getIssue(createdIssue.iid());
		IssueResponse persistedClosedIssue = IssueAssertions.assertSuccessfulIssueResponse(closedGetResponse, 200);
		assertEquals("closed", persistedClosedIssue.state(), "Closed state should persist");

		Response reopenResponse = issueService.updateIssue(createdIssue.iid(), UpdateIssueRequest.state("reopen"));
		IssueResponse reopenedIssue = IssueAssertions.assertSuccessfulIssueResponse(reopenResponse, 200);
		IssueAssertions.assertSameIdentity(reopenedIssue, createdIssue);
		IssueAssertions.assertState(reopenedIssue, "opened");

		Response reopenedGetResponse = issueService.getIssue(createdIssue.iid());
		IssueResponse persistedReopenedIssue = IssueAssertions.assertSuccessfulIssueResponse(reopenedGetResponse, 200);
		assertEquals("opened", persistedReopenedIssue.state(), "Reopened state should persist");
	}

	private IssueResponse createTrackedIssue() {
		CreateIssueRequest request = IssueTestData.validIssue();

		Response response = issueService.createIssue(request);

		IssueResponse issue = IssueAssertions.assertSuccessfulIssueResponse(response, 201);

		cleanup.track(issue.iid());

		return issue;
	}
}
