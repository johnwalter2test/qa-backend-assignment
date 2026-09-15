package com.abnamro.assignment.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.abnamro.assignment.assertions.IssueAssertions;
import com.abnamro.assignment.model.CreateIssueRequest;
import com.abnamro.assignment.model.IssueResponse;
import com.abnamro.assignment.model.UpdateIssueRequest;
import com.abnamro.assignment.service.IssueService;
import com.abnamro.assignment.support.IssueCleanup;
import com.abnamro.assignment.support.IssueTestData;

import io.restassured.response.Response;

@Tag("issues")
@Tag("state")
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
	@Tag("statetransistion")
	@DisplayName("Record issue close and reopen events in state history")
	void shouldCloseAndReopenIssue() {
		// state - close
		IssueResponse issue = createTrackedIssue(IssueTestData.validIssue());
		Response closeResponse = issueService.updateIssue(issue.iid(), UpdateIssueRequest.state("close"));
		assertEquals("closed", IssueAssertions.assertSuccessfulIssueResponse(closeResponse, 200).state());
 
		// state - reopen
		Response reopenResponse = issueService.updateIssue(issue.iid(), UpdateIssueRequest.state("reopen"));
		assertEquals("opened", IssueAssertions.assertSuccessfulIssueResponse(reopenResponse, 200).state());
		// state - close
		Response finalCloseResponse = issueService.updateIssue(issue.iid(), UpdateIssueRequest.state("close"));
		assertEquals("closed", IssueAssertions.assertSuccessfulIssueResponse(finalCloseResponse, 200).state());

		Response historyResponse = issueService.getIssueStateEvents(issue.iid());
		assertEquals(200, historyResponse.statusCode());
		List<String> states = historyResponse.jsonPath().getList("state", String.class);
		assertEquals(3, states.size(), "State history should contain exactly three transitions: " + states);
		assertTrue(states.contains("closed"), "State history should contain a closed event");
		assertTrue(states.contains("reopened"), "State history should contain a reopened event");
		long closedCount = states.stream().filter("closed"::equals).count();
		long reopenedCount = states.stream().filter("reopened"::equals).count();
		assertEquals(2, closedCount, "State history should contain two closed events");
		assertEquals(1, reopenedCount, "State history should contain one reopened event");
	}

	private IssueResponse createTrackedIssue(CreateIssueRequest request) {
		Response response = issueService.createIssue(request);

		IssueResponse issue = IssueAssertions.assertSuccessfulIssueResponse(response, 201);

		cleanup.track(issue.iid());

		return issue;
	}
}
