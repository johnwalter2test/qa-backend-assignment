package com.abnamro.assignment.config;

/** Holds API configuration. */
public final class ApiConfig {

	private static final String DEFAULT_BASE_URL = "https://gitlab.com/api/v4";

	private ApiConfig() {
		// Utility class - prevent instantiation
	}

	public static String baseUrl() {
		String baseUrl = getOptional("gitlab.base.url", "GITLAB_BASE_URL", DEFAULT_BASE_URL).replaceAll("/+$", "");
		return baseUrl.endsWith("/api/v4") ? baseUrl : baseUrl + "/api/v4";
	}

	public static long projectId() {
		String value = getRequired("gitlab.project.id", "GITLAB_PROJECT_ID");

		try {
			return Long.parseLong(value);
		} catch (NumberFormatException exception) {
			throw new IllegalStateException("GITLAB_PROJECT_ID must be a valid numeric project ID.", exception);
		}
	}

	public static String token() {
		return getRequired("gitlab.token", "GITLAB_TOKEN");
	}

	private static String getRequired(String systemProperty, String environmentVariable) {

		String value = resolve(systemProperty, environmentVariable);
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Missing required configuration: " + environmentVariable);
		}

		return value.trim();
	}

	private static String getOptional(String systemProperty, String environmentVariable, String defaultValue) {

		String value = resolve(systemProperty, environmentVariable);
		return value == null || value.isBlank() ? defaultValue : value.trim();
	}

	private static String resolve(String systemProperty, String environmentVariable) {

		String systemValue = System.getProperty(systemProperty);
		if (systemValue != null && !systemValue.isBlank()) {
			return systemValue;
		}

		return System.getenv(environmentVariable);
	}
}
