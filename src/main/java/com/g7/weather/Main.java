package com.g7.weather;

import com.g7.weather.exception.WeatherApiException;
import com.g7.weather.model.WeatherData;
import com.g7.weather.service.WeatherService;

/**
 * Main entry point for the G7 Weather Service application. Version 2: Supports
 * searching for any city via CLI arguments.
 */
public class Main {

    // Default API key (hardcoded)
    private static final String DEFAULT_API_KEY = "375ab58f17283ff241b4bf2098bc6f7b";

    public static void main(String[] args) {
        // Resolve API key: CLI argument > environment variable > default
        String apiKey = resolveApiKey(args);

        // Resolve city: Hardcoded for Version 1
        String city = "Ho Chi Minh City";

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║        G7 Weather Service v1.0.0         ║");
        System.out.println("║        Powered by OpenWeather API        ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();

        WeatherService service = new WeatherService(apiKey);

        try {
            System.out.printf("Fetching weather for: %s...%n%n", city);
            WeatherData data = service.getCurrentWeather(city);
            System.out.println(data);
        } catch (WeatherApiException e) {
            System.err.printf("Failed to fetch weather data (HTTP %d): %s%n",
                    e.getHttpStatusCode(), e.getMessage());
            System.exit(1);
        }
    }

    private static String resolveApiKey(String[] args) {
        if (args.length >= 1 && !args[0].isBlank()) {
            return args[0];
        }
        String envKey = System.getenv("OPENWEATHER_API_KEY");
        return (envKey != null) ? envKey : DEFAULT_API_KEY;
    }
}
