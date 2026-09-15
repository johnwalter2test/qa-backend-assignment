package com.abnamro.assignment.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents an issue creation request. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateIssueRequest(String title, String description, String labels, Boolean confidential,
		@JsonProperty("due_date") String dueDate, @JsonProperty("issue_type") String issueType) {

    

	/**
	 * Convenience constructor that creates a request with only title and description.
	 *
	 * @param title       issue title
	 * @param description issue description
	 */
	public CreateIssueRequest(String title, String description) {

		this(title, description, null, null, null, null);
	}

	/**
	 * Factory method to create a request with optional fields populated.
	 *
	 * @param title       issue title
	 * @param description issue description
	 * @param labels      comma-separated labels
	 * @param confidential confidential flag
	 * @param dueDate     due date string
	 * @param issueType   issue type
	 * @return new CreateIssueRequest instance
	 */
	public static CreateIssueRequest withOptionalFields(String title, String description, String labels,
			Boolean confidential, String dueDate, String issueType) {

		return new CreateIssueRequest(title, description, labels, confidential, dueDate, issueType);
	}
}
