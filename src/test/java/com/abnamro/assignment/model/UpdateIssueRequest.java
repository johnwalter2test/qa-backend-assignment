package com.abnamro.assignment.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents an issue update request. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateIssueRequest(

		String title, String description, @JsonProperty("state_event") String stateEvent,
		@JsonProperty("add_labels") String addLabels

) {

	/**
	 * Returns an update request that sets the title only.
	 *
	 * @param title new title
	 * @return update request
	 */
	public static UpdateIssueRequest title(String title) {
		return new UpdateIssueRequest(title, null, null, null);
	}

	/**
	 * Returns an update request that sets the description only.
	 *
	 * @param description new description
	 * @return update request
	 */
	public static UpdateIssueRequest description(String description) {
		return new UpdateIssueRequest(null, description, null, null);
	}

	/**
	 * Returns an update request that sets the state event (e.g. close/reopen).
	 *
	 * @param event state event name
	 * @return update request
	 */
	public static UpdateIssueRequest state(String event) {
		return new UpdateIssueRequest(null, null, event, null);
	}

	/**
	 * Returns an update request that adds labels.
	 *
	 * @param labels comma-separated labels
	 * @return update request
	 */
	public static UpdateIssueRequest addLabels(String labels) {
		return new UpdateIssueRequest(null, null, null, labels);
	}

	/**
	 * Returns an update request that sets both title and description.
	 *
	 * @param title       new title
	 * @param description new description
	 * @return update request
	 */
	public static UpdateIssueRequest titleAndDescription(String title, String description) {
		return new UpdateIssueRequest(title, description, null, null);
	}

	/**
	 * Returns an empty update request (no fields set).
	 *
	 * @return empty update request
	 */
	public static UpdateIssueRequest empty() {
		return new UpdateIssueRequest(null, null, null, null);
	}
}
