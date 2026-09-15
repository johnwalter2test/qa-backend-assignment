package com.abnamro.assignment.config;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/** Provides reusable request specifications. */
public final class RequestSpecFactory {

	private RequestSpecFactory() {
		// Utility class - prevent instantiation
	}

	/**
	 * Returns a RequestSpecification configured with the API base URL and a
	 * bearer Authorization header derived from {@link ApiConfig#token()}.
	 *
	 * @return authenticated request specification
	 */
	public static RequestSpecification authenticated() {
		return baseSpecification().addHeader("Authorization", "Bearer " + ApiConfig.token()).build();
	}

	/**
	 * Returns a RequestSpecification without an Authorization header. Use this
	 * for authentication negative tests.
	 *
	 * @return unauthenticated request specification
	 */
	public static RequestSpecification withoutAuthentication() {
		return baseSpecification().build();
	}

	/**
	 * Returns a RequestSpecification using the supplied bearer token.
	 *
	 * @param token bearer token string
	 * @return request specification with the provided Authorization header
	 */
	public static RequestSpecification withToken(String token) {
		return baseSpecification().addHeader("Authorization", "Bearer " + token).build();
	}

	private static RequestSpecBuilder baseSpecification() {
		return new RequestSpecBuilder().setBaseUri(ApiConfig.baseUrl()).setContentType(ContentType.JSON)
				.setAccept(ContentType.JSON);
	}
}
