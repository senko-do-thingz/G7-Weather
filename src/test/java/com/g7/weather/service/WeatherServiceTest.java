package com.g7.weather.service;

import com.g7.weather.exception.WeatherApiException;
import com.g7.weather.http.HttpHandler;
import com.g7.weather.http.SimpleHttpResponse;
import com.g7.weather.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WeatherService}.
 *
 * <p>Uses Mockito to mock {@link HttpHandler} (our own functional interface)
 * and constructs {@link SimpleHttpResponse} directly (plain POJO, no mocking
 * needed). This avoids the Java 25 restriction where Mockito cannot mock
 * platform classes/interfaces like {@code HttpClient} or {@code HttpResponse}.</p>
 */
@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    private static final String VALID_API_KEY = "test-api-key-12345";

    @Mock
    private HttpHandler mockHttpHandler;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService(VALID_API_KEY, mockHttpHandler);
    }

    // =========================================================================
    // Constructor Tests
    // =========================================================================

    @Nested
    @DisplayName("Constructor validation")
    class ConstructorTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when API key is null")
        void shouldThrowWhenApiKeyIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new WeatherService(null, mockHttpHandler));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when API key is blank")
        void shouldThrowWhenApiKeyIsBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> new WeatherService("   ", mockHttpHandler));
        }

        @Test
        @DisplayName("Should create service with valid API key")
        void shouldCreateWithValidApiKey() {
            assertDoesNotThrow(() -> new WeatherService(VALID_API_KEY, mockHttpHandler));
        }
    }

    // =========================================================================
    // Success Cases
    // =========================================================================

    @Nested
    @DisplayName("Successful API responses (HTTP 200)")
    class SuccessTests {

        @Test
        @DisplayName("Should return WeatherData when API returns valid JSON")
        void shouldReturnWeatherDataOnValidResponse() throws Exception {
            // Arrange
            String validJson = """
                    {
                        "main": {
                            "temp": 32.5,
                            "feels_like": 35.0,
                            "temp_min": 30.0,
                            "temp_max": 34.0,
                            "humidity": 70,
                            "pressure": 1010
                        },
                        "weather": [{
                            "id": 802,
                            "main": "Clouds",
                            "description": "scattered clouds",
                            "icon": "03d"
                        }],
                        "wind": {
                            "speed": 3.5,
                            "deg": 180
                        },
                        "name": "Ho Chi Minh City",
                        "cod": 200
                    }
                    """;

            SimpleHttpResponse response = new SimpleHttpResponse(200, validJson);
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act
            WeatherData result = weatherService.getCurrentWeather("Ho Chi Minh City");

            // Assert
            assertNotNull(result);
            assertEquals("Ho Chi Minh City", result.getName());
            assertEquals(200, result.getCod());
        }

        @Test
        @DisplayName("Should correctly parse temperature from API response")
        void shouldParseTemperatureCorrectly() throws Exception {
            // Arrange
            SimpleHttpResponse response = new SimpleHttpResponse(200, createValidWeatherJson(32.5, 70));
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act
            WeatherData result = weatherService.getCurrentWeather("Hanoi");

            // Assert
            assertNotNull(result.getMain(), "main section must not be null");
            assertEquals(32.5, result.getMain().getTemp(), 0.01);
        }

        @Test
        @DisplayName("Should correctly parse humidity from API response")
        void shouldParseHumidityCorrectly() throws Exception {
            // Arrange
            SimpleHttpResponse response = new SimpleHttpResponse(200, createValidWeatherJson(25.0, 85));
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act
            WeatherData result = weatherService.getCurrentWeather("Danang");

            // Assert
            assertNotNull(result.getMain().getHumidity(), "humidity must not be null");
            assertEquals(85, result.getMain().getHumidity());
        }

        @Test
        @DisplayName("Should parse weather description correctly")
        void shouldParseWeatherDescription() throws Exception {
            // Arrange
            SimpleHttpResponse response = new SimpleHttpResponse(200, createValidWeatherJson(28.0, 60));
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act
            WeatherData result = weatherService.getCurrentWeather("London");

            // Assert
            assertNotNull(result.getWeather());
            assertTrue(result.getWeather().length > 0, "weather array must not be empty");
            assertEquals("clear sky", result.getWeather()[0].getDescription());
        }

        @Test
        @DisplayName("main.temp should not be null on valid response")
        void mainTempShouldNotBeNull() throws Exception {
            // Arrange
            SimpleHttpResponse response = new SimpleHttpResponse(200, createValidWeatherJson(20.0, 50));
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act
            WeatherData result = weatherService.getCurrentWeather("Tokyo");

            // Assert — validates main.temp is not null
            assertNotNull(result.getMain().getTemp(),
                    "main.temp must not be null for a valid API response");
        }

        @Test
        @DisplayName("main.humidity should not be null on valid response")
        void mainHumidityShouldNotBeNull() throws Exception {
            // Arrange
            SimpleHttpResponse response = new SimpleHttpResponse(200, createValidWeatherJson(18.0, 45));
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act
            WeatherData result = weatherService.getCurrentWeather("Paris");

            // Assert — validates main.humidity is not null
            assertNotNull(result.getMain().getHumidity(),
                    "main.humidity must not be null for a valid API response");
        }
    }

    // =========================================================================
    // Error Cases — HTTP Status Codes
    // =========================================================================

    @Nested
    @DisplayName("HTTP error responses")
    class HttpErrorTests {

        @Test
        @DisplayName("Should throw WeatherApiException with code 404 when city not found")
        void shouldThrowOn404CityNotFound() throws Exception {
            // Arrange
            String errorBody = "{\"cod\":\"404\",\"message\":\"city not found\"}";
            SimpleHttpResponse response = new SimpleHttpResponse(404, errorBody);
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act & Assert
            WeatherApiException ex = assertThrows(WeatherApiException.class,
                    () -> weatherService.getCurrentWeather("NonExistentCity12345"));

            assertEquals(404, ex.getHttpStatusCode());
            assertTrue(ex.getMessage().contains("404"));
        }

        @Test
        @DisplayName("Should throw WeatherApiException with code 401 on invalid API key")
        void shouldThrowOn401Unauthorized() throws Exception {
            // Arrange
            String errorBody = "{\"cod\":401,\"message\":\"Invalid API key\"}";
            SimpleHttpResponse response = new SimpleHttpResponse(401, errorBody);
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act & Assert
            WeatherApiException ex = assertThrows(WeatherApiException.class,
                    () -> weatherService.getCurrentWeather("Hanoi"));

            assertEquals(401, ex.getHttpStatusCode());
        }

        @Test
        @DisplayName("Should throw WeatherApiException with code 500 on server error")
        void shouldThrowOn500InternalServerError() throws Exception {
            // Arrange
            SimpleHttpResponse response = new SimpleHttpResponse(500, "Internal Server Error");
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act & Assert
            WeatherApiException ex = assertThrows(WeatherApiException.class,
                    () -> weatherService.getCurrentWeather("Hanoi"));

            assertEquals(500, ex.getHttpStatusCode());
        }

        @Test
        @DisplayName("Should throw WeatherApiException with code 429 on rate limit")
        void shouldThrowOn429TooManyRequests() throws Exception {
            // Arrange
            SimpleHttpResponse response = new SimpleHttpResponse(429, "Rate limit exceeded");
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act & Assert
            WeatherApiException ex = assertThrows(WeatherApiException.class,
                    () -> weatherService.getCurrentWeather("Hanoi"));

            assertEquals(429, ex.getHttpStatusCode());
        }
    }

    // =========================================================================
    // Error Cases — Invalid / Malformed JSON
    // =========================================================================

    @Nested
    @DisplayName("Invalid JSON responses")
    class InvalidJsonTests {

        @Test
        @DisplayName("Should throw WeatherApiException on completely invalid JSON")
        void shouldThrowOnMalformedJson() throws Exception {
            // Arrange
            SimpleHttpResponse response = new SimpleHttpResponse(200, "THIS IS NOT JSON {{{");
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act & Assert
            assertThrows(WeatherApiException.class,
                    () -> weatherService.getCurrentWeather("Hanoi"));
        }

        @Test
        @DisplayName("Should throw WeatherApiException when JSON is missing 'main' section")
        void shouldThrowWhenMainSectionMissing() throws Exception {
            // Arrange — valid JSON but no "main" object
            String jsonMissingMain = """
                    {
                        "weather": [{"id": 800, "main": "Clear", "description": "clear sky"}],
                        "name": "Hanoi",
                        "cod": 200
                    }
                    """;
            SimpleHttpResponse response = new SimpleHttpResponse(200, jsonMissingMain);
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act & Assert
            WeatherApiException ex = assertThrows(WeatherApiException.class,
                    () -> weatherService.getCurrentWeather("Hanoi"));

            assertTrue(ex.getMessage().contains("main"));
        }

        @Test
        @DisplayName("Should throw WeatherApiException when main.temp is null")
        void shouldThrowWhenTempIsNull() throws Exception {
            // Arrange — "main" exists but "temp" is null
            String jsonNullTemp = """
                    {
                        "main": {
                            "humidity": 60,
                            "pressure": 1013
                        },
                        "name": "Hanoi",
                        "cod": 200
                    }
                    """;
            SimpleHttpResponse response = new SimpleHttpResponse(200, jsonNullTemp);
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act & Assert
            WeatherApiException ex = assertThrows(WeatherApiException.class,
                    () -> weatherService.getCurrentWeather("Hanoi"));

            assertTrue(ex.getMessage().contains("temp"));
        }

        @Test
        @DisplayName("Should throw WeatherApiException when main.humidity is null")
        void shouldThrowWhenHumidityIsNull() throws Exception {
            // Arrange — "main" exists but "humidity" is null
            String jsonNullHumidity = """
                    {
                        "main": {
                            "temp": 25.0,
                            "pressure": 1013
                        },
                        "name": "Hanoi",
                        "cod": 200
                    }
                    """;
            SimpleHttpResponse response = new SimpleHttpResponse(200, jsonNullHumidity);
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act & Assert
            WeatherApiException ex = assertThrows(WeatherApiException.class,
                    () -> weatherService.getCurrentWeather("Hanoi"));

            assertTrue(ex.getMessage().contains("humidity"));
        }

        @Test
        @DisplayName("Should throw WeatherApiException on empty response body")
        void shouldThrowOnEmptyResponseBody() throws Exception {
            // Arrange
            SimpleHttpResponse response = new SimpleHttpResponse(200, "");
            when(mockHttpHandler.send(any(HttpRequest.class))).thenReturn(response);

            // Act & Assert
            assertThrows(WeatherApiException.class,
                    () -> weatherService.getCurrentWeather("Hanoi"));
        }
    }

    // =========================================================================
    // Error Cases — Network / Infrastructure
    // =========================================================================

    @Nested
    @DisplayName("Network error scenarios")
    class NetworkErrorTests {

        @Test
        @DisplayName("Should throw WeatherApiException on IOException (network failure)")
        void shouldThrowOnNetworkFailure() throws Exception {
            // Arrange
            when(mockHttpHandler.send(any(HttpRequest.class)))
                    .thenThrow(new IOException("Connection refused"));

            // Act & Assert
            WeatherApiException ex = assertThrows(WeatherApiException.class,
                    () -> weatherService.getCurrentWeather("Hanoi"));

            assertEquals(0, ex.getHttpStatusCode());
            assertTrue(ex.getMessage().contains("Network error"));
        }

        @Test
        @DisplayName("Should throw WeatherApiException on InterruptedException")
        void shouldThrowOnInterruption() throws Exception {
            // Arrange
            when(mockHttpHandler.send(any(HttpRequest.class)))
                    .thenThrow(new InterruptedException("Thread interrupted"));

            // Act & Assert
            WeatherApiException ex = assertThrows(WeatherApiException.class,
                    () -> weatherService.getCurrentWeather("Hanoi"));

            assertEquals(0, ex.getHttpStatusCode());
            assertTrue(ex.getMessage().contains("interrupted"));

            // Verify thread interrupt flag was restored
            assertTrue(Thread.currentThread().isInterrupted());

            // Clean up interrupt flag
            Thread.interrupted();
        }
    }

    // =========================================================================
    // Input Validation Tests
    // =========================================================================

    @Nested
    @DisplayName("Input validation")
    class InputValidationTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when city is null")
        void shouldThrowOnNullCity() {
            assertThrows(IllegalArgumentException.class,
                    () -> weatherService.getCurrentWeather(null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when city is blank")
        void shouldThrowOnBlankCity() {
            assertThrows(IllegalArgumentException.class,
                    () -> weatherService.getCurrentWeather("   "));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when city is empty")
        void shouldThrowOnEmptyCity() {
            assertThrows(IllegalArgumentException.class,
                    () -> weatherService.getCurrentWeather(""));
        }
    }

    // =========================================================================
    // URL Building Tests
    // =========================================================================

    @Nested
    @DisplayName("URL building")
    class UrlBuildingTests {

        @Test
        @DisplayName("Should build correct URL for a simple city name")
        void shouldBuildCorrectUrl() {
            String url = weatherService.buildUrl("Hanoi");
            assertTrue(url.contains("q=Hanoi"));
            assertTrue(url.contains("appid=" + VALID_API_KEY));
            assertTrue(url.contains("units=metric"));
        }

        @Test
        @DisplayName("Should encode spaces in city names")
        void shouldEncodeSpacesInCityName() {
            String url = weatherService.buildUrl("Ho Chi Minh City");
            assertTrue(url.contains("q=Ho%20Chi%20Minh%20City"));
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Creates a valid OpenWeather API JSON response string with custom temp/humidity.
     */
    private String createValidWeatherJson(double temp, int humidity) {
        return String.format("""
                {
                    "main": {
                        "temp": %.1f,
                        "feels_like": %.1f,
                        "temp_min": %.1f,
                        "temp_max": %.1f,
                        "humidity": %d,
                        "pressure": 1013
                    },
                    "weather": [{
                        "id": 800,
                        "main": "Clear",
                        "description": "clear sky",
                        "icon": "01d"
                    }],
                    "wind": {
                        "speed": 2.5,
                        "deg": 90
                    },
                    "name": "TestCity",
                    "cod": 200
                }
                """, temp, temp + 2.0, temp - 2.0, temp + 3.0, humidity);
    }
}
