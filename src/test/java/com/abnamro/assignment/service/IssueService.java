package com.abnamro.assignment.service;

import static io.restassured.RestAssured.given;

import java.util.Map;

import com.abnamro.assignment.config.ApiConfig;
import com.abnamro.assignment.config.RequestSpecFactory;
import com.abnamro.assignment.model.CreateIssueRequest;
import com.abnamro.assignment.model.UpdateIssueRequest;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Provides operations for interacting with the GitLab Issues API.
 */
public final class IssueService {

	private static final String ISSUES_ENDPOINT = "/projects/{projectId}/issues";

	private static final String ISSUE_ENDPOINT = ISSUES_ENDPOINT + "/{issueIid}";

	/**
	 * Creates a new issue.
	 */
	public Response createIssue(CreateIssueRequest request) {
		return authenticatedRequest().body(request).when().post(ISSUES_ENDPOINT);
	}

	/**
	 * Retrieves an existing issue using its project-level IID.
	 */
	public Response getIssue(long issueIid) {
		return authenticatedRequest().pathParam("issueIid", issueIid).when().get(ISSUE_ENDPOINT);
	}

	// Updates an existing issue.

	public Response updateIssue(long issueIid, UpdateIssueRequest request) {
		return authenticatedRequest().pathParam("issueIid", issueIid).body(request).when().put(ISSUE_ENDPOINT);
	}

	// Deletes an existing issue.

	public Response deleteIssue(long issueIid) {
		return authenticatedRequest().pathParam("issueIid", issueIid).when().delete(ISSUE_ENDPOINT);
	}

	/**
	 * Retrieves all issues for the configured project.
	 */
	public Response listIssues() {
		return authenticatedRequest().when().get(ISSUES_ENDPOINT);
	}

	/**
	 * Allows a custom request specification for authentication tests, such as
	 * missing or invalid credentials.
	 */
	public Response listIssues(RequestSpecification specification) {
		return request(specification).when().get(ISSUES_ENDPOINT);
	}

	private RequestSpecification authenticatedRequest() {
		return request(RequestSpecFactory.authenticated());
	}

	private RequestSpecification request(RequestSpecification specification) {
		return given().spec(specification).pathParam("projectId", ApiConfig.projectId());
	}

	public Response listIssues(Map<String, ?> queryParams) {
		return authenticatedRequest().queryParams(queryParams).when().get(ISSUES_ENDPOINT);
	}

	public Response getIssueStateEvents(long issueIid) {
		return authenticatedRequest().pathParam("issueIid", issueIid).when()
				.get(ISSUE_ENDPOINT + "/resource_state_events");
	}

	public Response setTimeEstimate(long iid, String duration) {
		return authenticatedRequest().pathParam("issueIid", iid).queryParam("duration", duration).when()
				.post(ISSUES_ENDPOINT + "/{issueIid}/time_estimate");
	}

	public Response addSpentTime(long issueIid, String duration) {
		return authenticatedRequest().pathParam("issueIid", issueIid).queryParam("duration", duration).when()
				.post(ISSUES_ENDPOINT + "/{issueIid}/add_spent_time");
	}

	public Response resetSpentTime(long issueIid) {
		return authenticatedRequest().pathParam("issueIid", issueIid).when()
				.post(ISSUES_ENDPOINT + "/{issueIid}/reset_spent_time");
	}

	public Response getTimeStats(long issueIid) {
		return authenticatedRequest().pathParam("issueIid", issueIid).when()
				.get(ISSUES_ENDPOINT + "/{issueIid}/time_stats");
	}
}
