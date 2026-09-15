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

	/**
	 * Updates an existing issue.
	 *
	 * Sends a PUT to /projects/{projectId}/issues/{issueIid} with the supplied
	 * {@link UpdateIssueRequest} body.
	 *
	 * @param issueIid project-level IID of the issue to update
	 * @param request  update payload
	 * @return Rest Assured {@code Response} from the server
	 */
	public Response updateIssue(long issueIid, UpdateIssueRequest request) {
		return authenticatedRequest().pathParam("issueIid", issueIid).body(request).when().put(ISSUE_ENDPOINT);
	}

	/**
	 * Deletes an existing issue.
	 *
	 * Sends a DELETE to /projects/{projectId}/issues/{issueIid}. A successful
	 * deletion usually returns HTTP 204.
	 *
	 * @param issueIid project-level IID of the issue to delete
	 * @return Rest Assured {@code Response} from the server
	 */
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

	/**
	 * Creates an authenticated request specification.
	 */

	private RequestSpecification authenticatedRequest() {
		return request(RequestSpecFactory.authenticated());
	}

	/**
	 * Creates a request specification with the configured project ID.
	 */

	private RequestSpecification request(RequestSpecification specification) {
		return given().spec(specification).pathParam("projectId", ApiConfig.projectId());
	}

	/**
	 * Retrieves project issues using the supplied query parameters.
	 */

	public Response listIssues(Map<String, ?> queryParams) {
		return authenticatedRequest().queryParams(queryParams).when().get(ISSUES_ENDPOINT);
	}

	/**
	 * Retrieves the state transition events for an issue.
	 */

	public Response getIssueStateEvents(long issueIid) {
		return authenticatedRequest().pathParam("issueIid", issueIid).when()
				.get(ISSUE_ENDPOINT + "/resource_state_events");
	}

	/**
	 * Sets the time estimate for an issue.
	 */

	public Response setTimeEstimate(long iid, String duration) {
		return authenticatedRequest().pathParam("issueIid", iid).queryParam("duration", duration).when()
				.post(ISSUES_ENDPOINT + "/{issueIid}/time_estimate");
	}

	/**
	 * Adds spent time to an issue.
	 */

	public Response addSpentTime(long issueIid, String duration) {
		return authenticatedRequest().pathParam("issueIid", issueIid).queryParam("duration", duration).when()
				.post(ISSUES_ENDPOINT + "/{issueIid}/add_spent_time");
	}

	/**
	 * Resets the recorded spent time for an issue.
	 */

	public Response resetSpentTime(long issueIid) {
		return authenticatedRequest().pathParam("issueIid", issueIid).when()
				.post(ISSUES_ENDPOINT + "/{issueIid}/reset_spent_time");
	}

	/**
	 * Retrieves the time-tracking statistics for an issue.
	 */	

	public Response getTimeStats(long issueIid) {
		return authenticatedRequest().pathParam("issueIid", issueIid).when()
				.get(ISSUES_ENDPOINT + "/{issueIid}/time_stats");
	}
}
