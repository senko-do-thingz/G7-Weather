package com.g7.weather.http;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Default implementation of {@link HttpHandler} that delegates to
 * {@link java.net.http.HttpClient} and converts the response to
 * {@link SimpleHttpResponse}.
 */
public class DefaultHttpHandler implements HttpHandler {

    private final HttpClient httpClient;

    public DefaultHttpHandler() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public SimpleHttpResponse send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return new SimpleHttpResponse(response.statusCode(), response.body());
    }
}
