package com.abnamro.assignment.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.abnamro.assignment.assertions.IssueAssertions;
import com.abnamro.assignment.model.CreateIssueRequest;
import com.abnamro.assignment.model.IssueResponse;
import com.abnamro.assignment.model.TimeStatsResponse;
import com.abnamro.assignment.model.UpdateIssueRequest;
import com.abnamro.assignment.service.IssueService;
import com.abnamro.assignment.support.IssueCleanup;
import com.abnamro.assignment.support.IssueTestData;

import io.restassured.response.Response;

/** Groups issue edge case tests. */
@Tag("issues")
@Tag("edge-case")
public class IssueEdgeCaseTest {

	private IssueService issueService;
	private IssueCleanup cleanup;
	private static final int TOTAL_ISSUES = 101;
	private static final int MAX_PAGE_SIZE = 100;

	@Test
	@Tag("edge")
	@DisplayName("Preserve Unicode and special characters in issue content")
	void shouldPreserveUnicodeIssueContent() {
		CreateIssueRequest request = IssueTestData.unicodeIssue();
		Response createResponse = issueService.createIssue(request);
		IssueResponse createdIssue = IssueAssertions.assertSuccessfulIssueResponse(createResponse, 201);
		cleanup.track(createdIssue.iid());

		assertEquals(request.title(), createdIssue.title());
		assertEquals(request.description(), createdIssue.description());

		Response getResponse = issueService.getIssue(createdIssue.iid());
		IssueResponse persistedIssue = IssueAssertions.assertSuccessfulIssueResponse(getResponse, 200);

		assertEquals(request.title(), persistedIssue.title());
		assertEquals(request.description(), persistedIssue.description());
	}

	@Test
	@Tag("edge")
	@DisplayName("Allow different issues to use the same title")
	void shouldAllowDuplicateIssueTitles() {
		String sharedTitle = IssueTestData.uniqueTitle("DUPLICATE");

		IssueResponse firstIssue = createTrackedIssue(IssueTestData.duplicateTitleIssue(sharedTitle));

		IssueResponse secondIssue = createTrackedIssue(IssueTestData.duplicateTitleIssue(sharedTitle));

		assertEquals(sharedTitle, firstIssue.title());
		assertEquals(sharedTitle, secondIssue.title());

		assertNotEquals(firstIssue.iid(), secondIssue.iid(),
				"Issues with duplicate titles must still have unique IIDs");
	}

	@Test
	@Tag("edge")
	@Tag("pagination")
	@DisplayName("Paginate 101 issues across GitLab maximum page boundary")
	void shouldPaginateAcrossMaximumPageBoundary() {
		String marker = IssueTestData.uniqueTitle("PAGINATION");

		for (int i = 1; i <= TOTAL_ISSUES; i++) {
			CreateIssueRequest request = IssueTestData.issue(marker + "-" + i,
					IssueTestData.uniqueDescription("PAGINATION-" + i));

			createTrackedIssue(request);
		}
		List<IssueResponse> firstPage = getPage(marker, 1);
		List<IssueResponse> secondPage = getPage(marker, 2);
		List<IssueResponse> thirdPage = getPage(marker, 3);

		assertEquals(100, firstPage.size(), "Page 1 should contain the maximum 100 issues");
		assertEquals(1, secondPage.size(), "Page 2 should contain the remaining issue");
		assertEquals(0, thirdPage.size(), "Page 3 should be empty");
		assertEquals(TOTAL_ISSUES, firstPage.size() + secondPage.size(),
				"All 101 created issues should be returned across two pages");
	}

	@Test
	@Tag("list")
	@Tag("search")
	@DisplayName("Find an issue using a unique search marker")
	void shouldFindIssueBySearchMarker() {
		String marker = IssueTestData.uniqueTitle("SEARCH");
		IssueResponse createdIssue = createTrackedIssue(
				IssueTestData.issue(marker, IssueTestData.uniqueDescription("SEARCH")));
		Response response = issueService.listIssues(Map.of("search", marker));

		assertEquals(200, response.statusCode());
		List<IssueResponse> issues = Arrays.asList(response.as(IssueResponse[].class));
		assertTrue(issues.stream().anyMatch(issue -> issue.iid() == createdIssue.iid()),
				"Search should return the issue created by this test");
	}

	@Test
	@Tag("time-tracking")
	@DisplayName("Track and reset time for an issue")
	void shouldTrackAndResetIssueTime() {

		String title = IssueTestData.uniqueTitle("TimeTracking");
		CreateIssueRequest request = IssueTestData.issue(title, "Time tracking test");

		// Create issue
		Response createdIssueResponse = issueService.createIssue(request);
		assertEquals(201, createdIssueResponse.statusCode(), "Issue creation failed");
		IssueResponse createdIssue = createdIssueResponse.as(IssueResponse.class);

		long iid = createdIssue.iid();
		cleanup.track(iid);

		// Set estimate = 2h
		Response estimateResponse = issueService.setTimeEstimate(iid, "2h");
		assertEquals(200, estimateResponse.statusCode(), "Setting time estimate failed");
		TimeStatsResponse estimate = estimateResponse.as(TimeStatsResponse.class);
		assertEquals(7200, estimate.timeEstimate());

		// Add spent time = 30m
		Response spentTimeResponse = issueService.addSpentTime(iid, "30m");
		assertEquals(201, spentTimeResponse.statusCode(), "Adding spent time failed");
		TimeStatsResponse spentTime = spentTimeResponse.as(TimeStatsResponse.class);
		assertEquals(1800, spentTime.totalTimeSpent());

		// Get time stats
		Response timeStatsResponse = issueService.getTimeStats(iid);
		assertEquals(200, timeStatsResponse.statusCode(), "Getting time stats failed");
		TimeStatsResponse timeStats = timeStatsResponse.as(TimeStatsResponse.class);
		assertEquals(7200, timeStats.timeEstimate());
		assertEquals(1800, timeStats.totalTimeSpent());

		// Reset spent time
		Response resetResponse = issueService.resetSpentTime(iid);
		assertEquals(200, resetResponse.statusCode(), "Resetting spent time failed");
		TimeStatsResponse resetStats = resetResponse.as(TimeStatsResponse.class);
		assertEquals(0, resetStats.totalTimeSpent());

		// Verify values are actually persisted
		Response finalStatsResponse = issueService.getTimeStats(iid);
		assertEquals(200, finalStatsResponse.statusCode());
		TimeStatsResponse finalStats = finalStatsResponse.as(TimeStatsResponse.class);
		assertEquals(7200, finalStats.timeEstimate(), "Time estimate should remain unchanged");
		assertEquals(0, finalStats.totalTimeSpent(), "Spent time should be reset to zero");
	}

	@Test
	@DisplayName("Find an issue using a unique search marker")
	void shouldFindIssueBySearchMarkers() {

		String uniqueTitle = IssueTestData.uniqueTitle("TestFilter");
		CreateIssueRequest issue = IssueTestData.issue(uniqueTitle, "Test Description");
		Response createdissue = issueService.createIssue(issue);
		IssueResponse createdIssue = IssueAssertions.assertSuccessfulIssueResponse(createdissue, 201);
		cleanup.track(createdIssue.iid());
		Response resultissues = issueService.listIssues(Map.of("Search", uniqueTitle));
		List<IssueResponse> issues = Arrays.asList(resultissues.as(IssueResponse[].class));
		IssueResponse matchedIssue = issues.stream().filter(i -> i.title().equals(uniqueTitle)).findFirst()
				.orElseThrow();
		assertEquals(createdIssue.iid(), matchedIssue.iid());

		assertEquals(createdIssue.title(), matchedIssue.title());
	}

	private List<IssueResponse> getPage(String marker, int page) {

		Response response = issueService.listIssues(Map.of("search", marker, "page", page, "per_page", MAX_PAGE_SIZE,
				"order_by", "created_at", "sort", "asc"));

		assertEquals(200, response.statusCode(), "Pagination request should succeed");
		return Arrays.asList(response.as(IssueResponse[].class));
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

	private IssueResponse createTrackedIssue(CreateIssueRequest request) {
		Response response = issueService.createIssue(request);

		IssueResponse issue = IssueAssertions.assertSuccessfulIssueResponse(response, 201);

		cleanup.track(issue.iid());

		return issue;
	}
}
