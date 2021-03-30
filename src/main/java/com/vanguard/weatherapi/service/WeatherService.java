package com.vanguard.weatherapi.service;

import com.vanguard.weatherapi.dto.WeatherDto;
import com.vanguard.weatherapi.dto.openweather.OpenWeatherDataDto;
import com.vanguard.weatherapi.dto.openweather.WeatherData;
import com.vanguard.weatherapi.persistence.entity.Weather;
import com.vanguard.weatherapi.persistence.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherService {
    private final WeatherRepository weatherRepository;
    private final OpenWeatherService openWeatherService;

    @Value("${app.weather-data-expires-minutes}")
    private long dataExpireMinutes;

    public WeatherDto getWeather(String city, String country) {
        log.info("Getting weather for city {} of country {}", city, country);
        OffsetDateTime validBefore = OffsetDateTime.now().minus(dataExpireMinutes, ChronoUnit.MINUTES);
        Weather weather = weatherRepository.findTopByCityAndCountryOrderByUpdatedOnDesc(city, country);

        if (weather == null || weather.getUpdatedOn().isBefore(validBefore)) {
            weather = refreshWeatherData(city, country);
        }

        return WeatherDto.builder()
                .city(city)
                .country(country)
                .description(weather.getDescription())
                .build();
    }

    private Weather refreshWeatherData(String city, String country) {
        log.info("Refreshing weather for city {} of country {}", city, country);
        OpenWeatherDataDto openWeatherDataDto = openWeatherService.getWeather(city, country);

        Weather weather = Weather.builder()
                .city(city)
                .country(country)
                // If there are multiple weather items, join the descriptions by comma
                .description(openWeatherDataDto.getWeather().stream().map(WeatherData::getDescription)
                        .collect(Collectors.joining(",")))
                .updatedOn(OffsetDateTime.now())
                .build();
        return weatherRepository.save(weather);
    }
}
