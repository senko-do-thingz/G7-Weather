package com.g7.weather.exception;

/**
 * Custom exception for weather API-related errors.
 * Carries the HTTP status code for specific error handling.
 */
public class WeatherApiException extends Exception {

    private final int httpStatusCode;

    public WeatherApiException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public WeatherApiException(String message, int httpStatusCode, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}
