package com.g7.weather.service;

import com.g7.weather.exception.WeatherApiException;
import com.g7.weather.http.DefaultHttpHandler;
import com.g7.weather.http.HttpHandler;
import com.g7.weather.http.SimpleHttpResponse;
import com.g7.weather.model.WeatherData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

/**
 * Service class for interacting with the OpenWeather API.
 *
 * <p>Provides functionality to fetch current weather data for a given city
 * using the OpenWeatherMap Current Weather Data API.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 *   WeatherService service = new WeatherService("YOUR_API_KEY");
 *   WeatherData data = service.getCurrentWeather("Hanoi");
 *   System.out.println(data);
 * }</pre>
 *
 * @see <a href="https://openweathermap.org/current">OpenWeather Current Weather API</a>
 */
public class WeatherService {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final int TIMEOUT_SECONDS = 10;

    private final String apiKey;
    private final HttpHandler httpHandler;
    private final Gson gson;

    /**
     * Creates a WeatherService with the given API key and a default HttpHandler.
     *
     * @param apiKey OpenWeather API key
     */
    public WeatherService(String apiKey) {
        this(apiKey, new DefaultHttpHandler());
    }

    /**
     * Creates a WeatherService with a custom HttpHandler (useful for testing).
     *
     * @param apiKey      OpenWeather API key
     * @param httpHandler Custom HttpHandler instance
     */
    public WeatherService(String apiKey, HttpHandler httpHandler) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key must not be null or blank");
        }
        this.apiKey = apiKey;
        this.httpHandler = httpHandler;
        this.gson = new Gson();
    }

    /**
     * Fetches current weather data for the specified city.
     *
     * @param city The city name (e.g., "Hanoi", "London", "Ho Chi Minh City")
     * @return WeatherData object containing parsed weather information
     * @throws WeatherApiException if the API returns a non-200 status or the response cannot be parsed
     * @throws IllegalArgumentException if city is null or blank
     */
    public WeatherData getCurrentWeather(String city) throws WeatherApiException {
        validateCity(city);

        String url = buildUrl(city);
        SimpleHttpResponse response = executeRequest(url);
        return parseResponse(response);
    }

    /**
     * Fetches current weather data by geographic coordinates.
     *
     * @param lat Latitude
     * @param lon Longitude
     * @return WeatherData object containing parsed weather information
     * @throws WeatherApiException if the API returns a non-200 status or the response cannot be parsed
     */
    public WeatherData getCurrentWeatherByCoordinates(double lat, double lon) throws WeatherApiException {
        String url = String.format("%s?lat=%.4f&lon=%.4f&appid=%s&units=metric",
                BASE_URL, lat, lon, apiKey);
        SimpleHttpResponse response = executeRequest(url);
        return parseResponse(response);
    }

    // --- Internal methods ---

    /**
     * Validates the city parameter.
     */
    void validateCity(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City name must not be null or blank");
        }
    }

    /**
     * Builds the full API URL for a city query.
     */
    String buildUrl(String city) {
        return String.format("%s?q=%s&appid=%s&units=metric",
                BASE_URL, city.replace(" ", "%20"), apiKey);
    }

    /**
     * Executes the HTTP GET request and returns the response.
     */
    SimpleHttpResponse executeRequest(String url) throws WeatherApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .header("Accept", "application/json")
                    .build();

            return httpHandler.send(request);
        } catch (IOException e) {
            throw new WeatherApiException("Network error while calling OpenWeather API: " + e.getMessage(), 0, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeatherApiException("Request to OpenWeather API was interrupted", 0, e);
        }
    }

    /**
     * Parses the HTTP response body into a WeatherData object.
     * Validates the HTTP status code before parsing.
     */
    WeatherData parseResponse(SimpleHttpResponse response) throws WeatherApiException {
        int statusCode = response.statusCode();

        if (statusCode != 200) {
            throw new WeatherApiException(
                    String.format("OpenWeather API returned HTTP %d: %s", statusCode, response.body()),
                    statusCode
            );
        }

        try {
            WeatherData data = gson.fromJson(response.body(), WeatherData.class);

            if (data == null) {
                throw new WeatherApiException("Parsed weather data is null", 200);
            }

            validateWeatherData(data);
            return data;
        } catch (JsonSyntaxException e) {
            throw new WeatherApiException("Failed to parse JSON response: " + e.getMessage(), 200, e);
        }
    }

    /**
     * Validates that critical fields in the weather data are not null.
     */
    void validateWeatherData(WeatherData data) throws WeatherApiException {
        if (data.getMain() == null) {
            throw new WeatherApiException("Weather data is missing 'main' section", 200);
        }
        if (data.getMain().getTemp() == null) {
            throw new WeatherApiException("Weather data is missing 'main.temp' field", 200);
        }
        if (data.getMain().getHumidity() == null) {
            throw new WeatherApiException("Weather data is missing 'main.humidity' field", 200);
        }
    }
}
