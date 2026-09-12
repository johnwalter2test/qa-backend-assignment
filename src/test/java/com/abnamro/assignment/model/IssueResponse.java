package com.abnamro.assignment.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents an issue API response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IssueResponse(long id, long iid, @JsonProperty("project_id") long projectId,
                            String title, String description, String state,
                            @JsonProperty("created_at") String createdAt,
                            @JsonProperty("updated_at") String updatedAt,
                            @JsonProperty("web_url") String webUrl,
                            List<String> labels) {}
