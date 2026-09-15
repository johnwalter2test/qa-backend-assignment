package com.abnamro.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the time-tracking statistics of a GitLab issue.
 * Contains estimated time and total spent time in both seconds and human-readable format.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TimeStatsResponse(
		@JsonProperty("time_estimate") int timeEstimate,
		@JsonProperty("total_time_spent") int totalTimeSpent,
		@JsonProperty("human_time_estimate") String humanTimeEstimate,
		@JsonProperty("human_total_time_spent") String humanTotalTimeSpent

) {
}
