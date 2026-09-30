package com.g7.weather.http;

/**
 * Simple response object that wraps HTTP status code and body.
 *
 * <p>This replaces direct usage of {@code java.net.http.HttpResponse} in the
 * service layer, allowing tests to construct responses without mocking
 * sealed/platform classes that Mockito cannot handle on modern JDKs.</p>
 */
public class SimpleHttpResponse {

    private final int statusCode;
    private final String body;

    public SimpleHttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int statusCode() {
        return statusCode;
    }

    public String body() {
        return body;
    }
}
