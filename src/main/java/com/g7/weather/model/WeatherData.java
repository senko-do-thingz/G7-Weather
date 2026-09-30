package com.g7.weather.model;

/**
 * Data model representing weather data returned from the OpenWeather API.
 *
 * Maps to the JSON structure:
 * {
 *   "main": { "temp": 25.5, "humidity": 60, "pressure": 1013, "feels_like": 26.0 },
 *   "weather": [{ "id": 800, "main": "Clear", "description": "clear sky", "icon": "01d" }],
 *   "name": "Ho Chi Minh City",
 *   "cod": 200
 * }
 */
public class WeatherData {

    private Main main;
    private Weather[] weather;
    private String name;
    private int cod;
    private Wind wind;

    // --- Nested classes mapping JSON structure ---

    public static class Main {
        private Double temp;
        private Double feels_like;
        private Double temp_min;
        private Double temp_max;
        private Integer humidity;
        private Integer pressure;

        public Double getTemp() { return temp; }
        public void setTemp(Double temp) { this.temp = temp; }

        public Double getFeelsLike() { return feels_like; }
        public void setFeelsLike(Double feelsLike) { this.feels_like = feelsLike; }

        public Double getTempMin() { return temp_min; }
        public void setTempMin(Double tempMin) { this.temp_min = tempMin; }

        public Double getTempMax() { return temp_max; }
        public void setTempMax(Double tempMax) { this.temp_max = tempMax; }

        public Integer getHumidity() { return humidity; }
        public void setHumidity(Integer humidity) { this.humidity = humidity; }

        public Integer getPressure() { return pressure; }
        public void setPressure(Integer pressure) { this.pressure = pressure; }
    }

    public static class Weather {
        private int id;
        private String main;
        private String description;
        private String icon;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getMain() { return main; }
        public void setMain(String main) { this.main = main; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }

    public static class Wind {
        private Double speed;
        private Integer deg;

        public Double getSpeed() { return speed; }
        public void setSpeed(Double speed) { this.speed = speed; }

        public Integer getDeg() { return deg; }
        public void setDeg(Integer deg) { this.deg = deg; }
    }

    // --- Getters & Setters ---

    public Main getMain() { return main; }
    public void setMain(Main main) { this.main = main; }

    public Weather[] getWeather() { return weather; }
    public void setWeather(Weather[] weather) { this.weather = weather; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCod() { return cod; }
    public void setCod(int cod) { this.cod = cod; }

    public Wind getWind() { return wind; }
    public void setWind(Wind wind) { this.wind = wind; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Weather Report ===\n");
        sb.append("City: ").append(name).append("\n");

        if (main != null) {
            sb.append("Temperature: ").append(main.getTemp()).append("°C\n");
            sb.append("Feels Like:  ").append(main.getFeelsLike()).append("°C\n");
            sb.append("Humidity:    ").append(main.getHumidity()).append("%\n");
            sb.append("Pressure:    ").append(main.getPressure()).append(" hPa\n");
        }

        if (weather != null && weather.length > 0) {
            sb.append("Condition:   ").append(weather[0].getDescription()).append("\n");
        }

        if (wind != null) {
            sb.append("Wind Speed:  ").append(wind.getSpeed()).append(" m/s\n");
        }

        return sb.toString();
    }
}
