package com.abnamro.assignment.config;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/** Provides reusable request specifications. */
public final class RequestSpecFactory {

	private RequestSpecFactory() {
		// Utility class - prevent instantiation
	}

	public static RequestSpecification authenticated() {
		return baseSpecification().addHeader("Authorization", "Bearer " + ApiConfig.token()).build();
	}

	public static RequestSpecification withoutAuthentication() {
		return baseSpecification().build();
	}

	public static RequestSpecification withToken(String token) {
		return baseSpecification().addHeader("Authorization", "Bearer " + token).build();
	}

	private static RequestSpecBuilder baseSpecification() {
		return new RequestSpecBuilder().setBaseUri(ApiConfig.baseUrl()).setContentType(ContentType.JSON)
				.setAccept(ContentType.JSON);
	}
}
