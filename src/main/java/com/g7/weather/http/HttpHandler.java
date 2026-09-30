package com.g7.weather.http;

import java.io.IOException;
import java.net.http.HttpRequest;

/**
 * Abstraction over HTTP communication to allow easy testing.
 *
 * <p>On modern JDKs (17+/25), Mockito cannot mock platform classes
 * like {@code HttpClient} or {@code HttpResponse}. This interface
 * returns our own {@link SimpleHttpResponse} to avoid that problem.</p>
 */
@FunctionalInterface
public interface HttpHandler {

    /**
     * Sends an HTTP request and returns a simplified response.
     *
     * @param request the HTTP request to send
     * @return a SimpleHttpResponse containing status code and body
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    SimpleHttpResponse send(HttpRequest request) throws IOException, InterruptedException;
}
