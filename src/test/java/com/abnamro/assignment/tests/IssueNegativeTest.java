package com.abnamro.assignment.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.restassured.response.Response;

@Tag("issues")
@Tag("negative")
class IssueNegativeTest {

	private IssueService issueService;
	private IssueCleanup cleanup;

	@Test
	@Tag("negative")
	@DisplayName("Return not found when retrieving a deleted issue")
	void shouldReturnNotFoundForDeletedIssue() {

		CreateIssueRequest request = IssueTestData.validIssue();
		Response createResponse = issueService.createIssue(request);

		IssueResponse createdIssue = IssueAssertions.assertSuccessfulIssueResponse(createResponse, 201);
		cleanup.track(createdIssue.iid());
		Response deleteResponse = issueService.deleteIssue(createdIssue.iid());
		assertEquals(204, deleteResponse.statusCode(), "Issue should be deleted successfully");

		Response getResponse = issueService.getIssue(createdIssue.iid());
		assertEquals(404, getResponse.statusCode(), "Deleted issue should return 404");
	}

	@Test
	@Tag("negative")
	@DisplayName("Reject issue creation when mandatory title is missing")
	void shouldRejectIssueWithoutTitle() {
		CreateIssueRequest request = IssueTestData.issueWithoutTitle();

		Response response = issueService.createIssue(request);
		System.out.println("Status: " + response.statusCode());
		System.out.println("Body: " + response.asPrettyString());
		assertEquals(400, response.statusCode(), "Issue without title should be rejected");
		assertEquals("title is missing", response.jsonPath().getString("error"));
	}

	@Test
	@Tag("negative")
	@DisplayName("Reject issue creation when title is empty")
	void shouldRejectIssueWithEmptyTitle() {

		CreateIssueRequest request = IssueTestData.issueWithEmptyTitle();
		Response response = issueService.createIssue(request);
		assertEquals(400, response.statusCode(), "Issue with empty title should be rejected");
		assertNotNull(response.jsonPath().get("message"), "Validation error should contain a message");
	}

	@Test
	@Tag("negative")
	@DisplayName("Reject update when no fields are supplied")
	void shouldRejectEmptyUpdate() {
		IssueResponse createdIssue = createTrackedIssue();

		Response response = issueService.updateIssue(createdIssue.iid(), IssueTestData.emptyUpdate());
		assertEquals(400, response.statusCode(), "Update without any fields should return 400");
	}

	@Test
	@Tag("negative")
	@DisplayName("Reject update with unsupported issue state event")
	void shouldRejectInvalidStateEvent() {
		IssueResponse createdIssue = createTrackedIssue();
		Response response = issueService.updateIssue(createdIssue.iid(), IssueTestData.invalidStateUpdate());
		assertEquals(400, response.statusCode(), "Unsupported state_event should return 400");

		Response getResponse = issueService.getIssue(createdIssue.iid());
		IssueResponse persistedIssue = IssueAssertions.assertSuccessfulIssueResponse(getResponse, 200);
		assertEquals("opened", persistedIssue.state(), "Rejected update must not change the issue state");
	}

	@Test
	@Tag("negative")
	@DisplayName("Return not found when updating an issue that has already been deleted")
	void shouldReturnNotFoundWhenUpdatingDeletedIssue() {
		IssueResponse createdIssue = createTrackedIssue();

		Response deleteResponse = issueService.deleteIssue(createdIssue.iid());
		assertEquals(204, deleteResponse.statusCode(), "Precondition: issue should be deleted");

		Response updateResponse = issueService.updateIssue(createdIssue.iid(),
				UpdateIssueRequest.title(IssueTestData.uniqueTitle("AFTER-DELETE")));
		assertEquals(404, updateResponse.statusCode(), "Deleted issue should not be updateable");
	}

	@Test
	@Tag("negative")
	@DisplayName("Return not found when deleting an issue twice")
	void shouldReturnNotFoundWhenDeletingIssueTwice() {
		IssueResponse createdIssue = createTrackedIssue();

		Response firstDelete = issueService.deleteIssue(createdIssue.iid());
		assertEquals(204, firstDelete.statusCode(), "First deletion should succeed");

		Response secondDelete = issueService.deleteIssue(createdIssue.iid());
		assertEquals(404, secondDelete.statusCode(), "Deleting an already deleted issue should return 404");
	}

	@BeforeEach
	void setUp() {
		issueService = new IssueService();
		cleanup = new IssueCleanup(issueService);
	}

	@AfterEach
	void tearDown() {
		cleanup.cleanup();
	}

	private IssueResponse createTrackedIssue() {
		CreateIssueRequest request = IssueTestData.validIssue();

		Response response = issueService.createIssue(request);
		IssueResponse issue = IssueAssertions.assertSuccessfulIssueResponse(response, 201);
		cleanup.track(issue.iid());

		return issue;
	}
}
