package com.vanguard.weatherapi.service;

import com.vanguard.weatherapi.dto.WeatherDto;
import com.vanguard.weatherapi.dto.openweather.OpenWeatherDataDto;
import com.vanguard.weatherapi.dto.openweather.WeatherData;
import com.vanguard.weatherapi.persistence.entity.Weather;
import com.vanguard.weatherapi.persistence.repository.WeatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
public class WeatherServiceTest {
    private static final String city = "Melbourne";
    private static final String country = "Australia";

    @MockBean
    private WeatherRepository weatherRepository;
    @MockBean
    private OpenWeatherService openWeatherService;
    private WeatherService weatherService;

    @BeforeEach
    public void setup() {
        weatherService = new WeatherService(weatherRepository, openWeatherService);
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(weatherService, "dataExpireMinutes", 2);

        Mockito.when(weatherRepository.save(any(Weather.class))).thenAnswer(f -> f.getArgument(0));
    }

    @Test
    public void shouldGetWeatherFromRepository() {
        String description = "Sunny";
        Weather weather = Weather.builder()
                .city(city)
                .country(country)
                .description(description)
                .updatedOn(OffsetDateTime.now())
                .build();

        Mockito.when(weatherRepository.findTopByCityAndCountryOrderByUpdatedOnDesc(eq(city), eq(country)))
                .thenReturn(weather);

        WeatherDto weatherDto = weatherService.getWeather(city, country);
        assertNotNull(weatherDto);
        assertEquals(city, weatherDto.getCity());
        assertEquals(country, weatherDto.getCountry());
        assertEquals(description, weatherDto.getDescription());
    }

    @Test
    public void shouldGetWeatherWhenDataOutOfDate() {
        String description = "Cloudy";
        Weather weather = Weather.builder()
                .city(city)
                .country(country)
                .description("Sunny")
                .updatedOn(OffsetDateTime.now().minus(2L, ChronoUnit.MINUTES))
                .build();

        OpenWeatherDataDto newWeather = OpenWeatherDataDto.builder()
                .weather(Collections.singletonList(new WeatherData(description)))
                .build();

        Mockito.when(weatherRepository.findTopByCityAndCountryOrderByUpdatedOnDesc(eq(city), eq(country)))
                .thenReturn(weather);

        Mockito.when(openWeatherService.getWeather(eq(city), eq(country))).thenReturn(newWeather);

        WeatherDto weatherDto = weatherService.getWeather(city, country);
        assertNotNull(weatherDto);
        assertEquals(city, weatherDto.getCity());
        assertEquals(country, weatherDto.getCountry());
        assertEquals(description, weatherDto.getDescription());
    }

    @Test
    public void shouldGetWeatherWhenNotExistsInRepository() {
        String description = "Cloudy";

        OpenWeatherDataDto newWeather = OpenWeatherDataDto.builder()
                .weather(Collections.singletonList(new WeatherData(description)))
                .build();

        Mockito.when(weatherRepository.findTopByCityAndCountryOrderByUpdatedOnDesc(eq(city), eq(country)))
                .thenReturn(null);

        Mockito.when(openWeatherService.getWeather(eq(city), eq(country))).thenReturn(newWeather);

        WeatherDto weatherDto = weatherService.getWeather(city, country);
        assertNotNull(weatherDto);
        assertEquals(city, weatherDto.getCity());
        assertEquals(country, weatherDto.getCountry());
        assertEquals(description, weatherDto.getDescription());
    }
}
