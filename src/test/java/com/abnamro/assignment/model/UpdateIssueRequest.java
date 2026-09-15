package com.abnamro.assignment.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents an issue update request. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateIssueRequest(

		String title, String description, @JsonProperty("state_event") String stateEvent,
		@JsonProperty("add_labels") String addLabels

) {

	public static UpdateIssueRequest title(String title) {
		return new UpdateIssueRequest(title, null, null, null);
	}

	public static UpdateIssueRequest description(String description) {
		return new UpdateIssueRequest(null, description, null, null);
	}

	public static UpdateIssueRequest state(String event) {
		return new UpdateIssueRequest(null, null, event, null);
	}

	public static UpdateIssueRequest addLabels(String labels) {
		return new UpdateIssueRequest(null, null, null, labels);
	}

	public static UpdateIssueRequest titleAndDescription(String title, String description) {
		return new UpdateIssueRequest(title, description, null, null);
	}

	public static UpdateIssueRequest empty() {
		return new UpdateIssueRequest(null, null, null, null);
	}
}
