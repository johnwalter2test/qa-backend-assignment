package com.abnamro.assignment.tests;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import io.restassured.response.Response;

/**
 * Verifies CRUD behavior of the GitLab Issues API.
 */
class IssueCrudTest {

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
	@DisplayName("Create an issue and retrieve the persisted issue")
	void shouldCreateAndRetrieveIssue() {
		CreateIssueRequest request = IssueTestData.validIssue();

		Response createResponse = issueService.createIssue(request);
		IssueResponse createdIssue = IssueAssertions.assertSuccessfulIssueResponse(createResponse, 201);
		cleanup.track(createdIssue.iid());
		IssueAssertions.assertCreatedIssue(createdIssue, request);

		Response getResponse = issueService.getIssue(createdIssue.iid());
		IssueResponse retrievedIssue = IssueAssertions.assertSuccessfulIssueResponse(getResponse, 200);
		IssueAssertions.assertSameIdentity(retrievedIssue, createdIssue);
		assertEquals(createdIssue.title(), retrievedIssue.title(), "Retrieved title should match created title");
		assertEquals(createdIssue.description(), retrievedIssue.description(),
				"Retrieved description should match created description");
	}

	@Test
	@DisplayName("Update an existing issue and verify the changes are persisted")
	void shouldUpdateIssue() {
		CreateIssueRequest createRequest = IssueTestData.validIssue();
		Response createResponse = issueService.createIssue(createRequest);
		IssueResponse createdIssue = IssueAssertions.assertSuccessfulIssueResponse(createResponse, 201);
		cleanup.track(createdIssue.iid());

		String updatedTitle = IssueTestData.uniqueTitle("UPDATE");
		String updatedDescription = IssueTestData.uniqueDescription("UPDATE");
		UpdateIssueRequest updateRequest = UpdateIssueRequest.titleAndDescription(updatedTitle, updatedDescription);

		Response updateResponse = issueService.updateIssue(createdIssue.iid(), updateRequest);
		IssueResponse updatedIssue = IssueAssertions.assertSuccessfulIssueResponse(updateResponse, 200);
		IssueAssertions.assertSameIdentity(updatedIssue, createdIssue);
		assertEquals(updatedTitle, updatedIssue.title(), "Updated title should be returned");
		assertEquals(updatedDescription, updatedIssue.description(), "Updated description should be returned");

		Response getResponse = issueService.getIssue(createdIssue.iid());
		IssueResponse persistedIssue = IssueAssertions.assertSuccessfulIssueResponse(getResponse, 200);
		assertEquals(updatedTitle, persistedIssue.title(), "Updated title should persist");
		assertEquals(updatedDescription, persistedIssue.description(), "Updated description should persist");
	}

	@Test
	@DisplayName("Delete an existing issue and verify it can no longer be retrieved")
	void shouldDeleteIssue() {

		CreateIssueRequest createRequest = IssueTestData.validIssue();
		Response createResponse = issueService.createIssue(createRequest);
		IssueResponse createdIssue = IssueAssertions.assertSuccessfulIssueResponse(createResponse, 201);
		cleanup.track(createdIssue.iid());

		Response deleteResponse = issueService.deleteIssue(createdIssue.iid());
		assertEquals(204, deleteResponse.statusCode(), "Deleting an existing issue should return 204");

		Response getResponse = issueService.getIssue(createdIssue.iid());
		assertEquals(404, getResponse.statusCode(), "Deleted issue should no longer be retrievable");

	}

}
