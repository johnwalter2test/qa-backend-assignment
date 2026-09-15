package com.abnamro.assignment.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

class IssueEndToEndTest {

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
	@Tag("e2e")
	@DisplayName("Complete issue lifecycle with full fields, filtering and pagination")
	void shouldCompleteFullIssueLifecycleEndToEnd() {

		String marker = IssueTestData.uniqueTitle("E2E");
		String dueDate = LocalDate.now().plusDays(30).toString();
       //1. CREATE ISSUE WITH OPTIONAL FIELDS
		CreateIssueRequest createRequest = IssueTestData.fullFieldIssue(marker + "-PRIMARY",
				IssueTestData.uniqueDescription("E2E"), "Regression", true, dueDate, "issue");

		IssueResponse createdIssue = createTrackedIssue(createRequest);

		assertEquals(createRequest.title(), createdIssue.title());
		assertEquals(createRequest.description(), createdIssue.description());
		assertEquals(true, createdIssue.confidential());
		assertEquals(dueDate, createdIssue.dueDate());
		assertEquals("issue", createdIssue.issueType());
		assertEquals("opened", createdIssue.state());
		assertTrue(createdIssue.labels().contains("Regression"), "Created issue labels: " + createdIssue.labels());

		//  RETRIEVE CREATED ISSUE

		Response getResponse = issueService.getIssue(createdIssue.iid());

		IssueResponse retrievedIssue = IssueAssertions.assertSuccessfulIssueResponse(getResponse, 200);

		IssueAssertions.assertSameIdentity(retrievedIssue, createdIssue);

		assertEquals(createRequest.title(), retrievedIssue.title());

		assertEquals(createRequest.description(), retrievedIssue.description());

		// UPDATE TITLE AND DESCRIPTION

		String updatedTitle = marker + "-PRIMARY-UPDATED";
		String updatedDescription = IssueTestData.uniqueDescription("E2E-UPDATED");

		Response updateResponse = issueService.updateIssue(createdIssue.iid(),
				UpdateIssueRequest.titleAndDescription(updatedTitle, updatedDescription));
		IssueResponse updatedIssue = IssueAssertions.assertSuccessfulIssueResponse(updateResponse, 200);

		assertEquals(updatedTitle, updatedIssue.title());
		assertEquals(updatedDescription, updatedIssue.description());

	   // ADD ANOTHER LABEL

		Response labelResponse = issueService.updateIssue(createdIssue.iid(), UpdateIssueRequest.addLabels("Smoke"));
		IssueResponse labelledIssue = IssueAssertions.assertSuccessfulIssueResponse(labelResponse, 200);
		Set<String> expectedLabels = Set.of("Regression", "Smoke");
		assertEquals(expectedLabels, new HashSet<>(labelledIssue.labels()));

		// CLOSE ISSUE

		Response closeResponse = issueService.updateIssue(createdIssue.iid(), UpdateIssueRequest.state("close"));

		IssueResponse closedIssue = IssueAssertions.assertSuccessfulIssueResponse(closeResponse, 200);

		IssueAssertions.assertState(closedIssue, "closed");

		// FILTER CLOSED ISSUES

		Map<String, Object> closedFilter = new HashMap<>();

		closedFilter.put("state", "closed");

		closedFilter.put("search", marker);

		closedFilter.put("per_page", 10);

		Response filterResponse = issueService.listIssues(closedFilter);
		assertEquals(200, filterResponse.statusCode());

		List<IssueResponse> closedIssues = Arrays.asList(filterResponse.as(IssueResponse[].class));
		assertTrue(closedIssues.stream().anyMatch(issue -> issue.iid() == createdIssue.iid()),
				"Closed filter should return the E2E issue");
		assertTrue(closedIssues.stream().allMatch(issue -> "closed".equals(issue.state())),
				"All filtered issues should be closed");

		// VERIFY STATE HISTORY

		Response historyResponse = issueService.getIssueStateEvents(createdIssue.iid());
		assertEquals(200, historyResponse.statusCode());
		List<String> states = historyResponse.jsonPath().getList("state", String.class);
		assertTrue(states.contains("closed"), "State history should contain closed event");

	   // CREATE MORE ISSUES FOR PAGINATION 

		IssueResponse secondIssue = createTrackedIssue(
				IssueTestData.issue(marker + "-PAGE-2", IssueTestData.uniqueDescription("E2E-PAGE-2")));

		IssueResponse thirdIssue = createTrackedIssue(
				IssueTestData.issue(marker + "-PAGE-3", IssueTestData.uniqueDescription("E2E-PAGE-3")));

		// PAGINATION - PAGE 1
		
		
		Map<String, Object> pageOneParams = new HashMap<>();

		pageOneParams.put("search", marker);
		pageOneParams.put("state", "all");
		pageOneParams.put("order_by", "created_at");
		pageOneParams.put("sort", "asc");
		pageOneParams.put("per_page", 2);
		pageOneParams.put("page", 1);

		Response pageOneResponse = issueService.listIssues(pageOneParams);

		assertEquals(200, pageOneResponse.statusCode());

		List<IssueResponse> pageOne = Arrays.asList(pageOneResponse.as(IssueResponse[].class));
		assertEquals(2, pageOne.size(), "First page should contain two issues");

		// PAGINATION - PAGE 2

		Map<String, Object> pageTwoParams = new HashMap<>(pageOneParams);
		pageTwoParams.put("page", 2);
		Response pageTwoResponse = issueService.listIssues(pageTwoParams);
		assertEquals(200, pageTwoResponse.statusCode());
		List<IssueResponse> pageTwo = Arrays.asList(pageTwoResponse.as(IssueResponse[].class));
		assertEquals(1, pageTwo.size(), "Second page should contain the remaining issue");

		// PAGINATION - PAGE 3

		Map<String, Object> pageThreeParams = new HashMap<>(pageOneParams);
		pageThreeParams.put("page", 3);
		Response pageThreeResponse = issueService.listIssues(pageThreeParams);
		assertEquals(200, pageThreeResponse.statusCode());
		List<IssueResponse> pageThree = Arrays.asList(pageThreeResponse.as(IssueResponse[].class));
		assertTrue(pageThree.isEmpty(), "Third page should be empty");

		/*
		 * ------------------------------------------------- 12. VERIFY PAGINATION DID
		 * NOT LOSE DATA -------------------------------------------------
		 */

		List<IssueResponse> pagedIssues = new ArrayList<>();

		pagedIssues.addAll(pageOne);
		pagedIssues.addAll(pageTwo);

		assertEquals(3, pagedIssues.size());

		Set<Long> returnedIids = new HashSet<>();

		for (IssueResponse issue : pagedIssues) {
			returnedIids.add(issue.iid());
		}

		assertTrue(returnedIids.contains(createdIssue.iid()));

		assertTrue(returnedIids.contains(secondIssue.iid()));

		assertTrue(returnedIids.contains(thirdIssue.iid()));

		// REOPEN PRIMARY ISSUE

		Response reopenResponse = issueService.updateIssue(createdIssue.iid(), UpdateIssueRequest.state("reopen"));

		IssueResponse reopenedIssue = IssueAssertions.assertSuccessfulIssueResponse(reopenResponse, 200);
		IssueAssertions.assertState(reopenedIssue, "opened");

	  //  FINAL GET - VERIFY

		Response finalGetResponse = issueService.getIssue(createdIssue.iid());
		IssueResponse finalIssue = IssueAssertions.assertSuccessfulIssueResponse(finalGetResponse, 200);
		assertEquals(updatedTitle, finalIssue.title());
		assertEquals(updatedDescription, finalIssue.description());
		assertEquals(expectedLabels, new HashSet<>(finalIssue.labels()));
		assertEquals(true, finalIssue.confidential());
		assertEquals(dueDate, finalIssue.dueDate());
		assertEquals("issue", finalIssue.issueType());
		assertEquals("opened", finalIssue.state());

		// DELETE PRIMARY ISSUE

		Response deleteResponse = issueService.deleteIssue(createdIssue.iid());
		assertEquals(204, deleteResponse.statusCode());
		Response deletedGetResponse = issueService.getIssue(createdIssue.iid());
		assertEquals(404, deletedGetResponse.statusCode());
	}

	private IssueResponse createTrackedIssue(CreateIssueRequest request) {

		Response response = issueService.createIssue(request);

		IssueResponse issue = IssueAssertions.assertSuccessfulIssueResponse(response, 201);

		cleanup.track(issue.iid());

		return issue;
	}
}
