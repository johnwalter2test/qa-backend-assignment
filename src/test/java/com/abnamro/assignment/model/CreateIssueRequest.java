package com.abnamro.assignment.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents an issue creation request. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateIssueRequest(String title, String description, String labels, Boolean confidential,
		@JsonProperty("due_date") String dueDate, @JsonProperty("issue_type") String issueType) {

	public CreateIssueRequest(String title, String description) {

		this(title, description, null, null, null, null);
	}

	public static CreateIssueRequest withOptionalFields(String title, String description, String labels,
			Boolean confidential, String dueDate, String issueType) {

		return new CreateIssueRequest(title, description, labels, confidential, dueDate, issueType);
	}
}
