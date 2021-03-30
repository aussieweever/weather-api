package com.vanguard.weatherapi.service;

import com.vanguard.weatherapi.dto.openweather.OpenWeatherDataDto;
import com.vanguard.weatherapi.dto.openweather.WeatherData;
import com.vanguard.weatherapi.exception.CityNotFoundException;
import com.vanguard.weatherapi.exception.OpenWeatherApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
public class OpenWeatherServiceTest {
    private static final String API_URL = "http://localhost:5000/weather";
    private static final String API_KEY = "fakeApiKey";
    private static final String UNITS_VALUE = "metric";
    private OpenWeatherService openWeatherService;
    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        openWeatherService = new OpenWeatherService(restTemplate);

        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(openWeatherService, "apiKey", API_KEY);
        ReflectionTestUtils.setField(openWeatherService, "apiUrl", API_URL);
        ReflectionTestUtils.setField(openWeatherService, "units", UNITS_VALUE);
    }

    @Test
    public void shouldGetWeatherData() {
        String city = "Melbourne";
        String country = "Australia";
        OpenWeatherDataDto expectedResult = OpenWeatherDataDto.builder()
                .weather(Arrays.asList(
                        new WeatherData("Cloudy"),
                        new WeatherData("Clear")
                )).build();
        String requestUrl = String.format("%s?q=%s,%s&appid=%s&units=%s", API_URL, city, country, API_KEY, UNITS_VALUE);

        Mockito.when(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.GET), any(), eq(OpenWeatherDataDto.class)))
                .thenReturn(new ResponseEntity<OpenWeatherDataDto>(expectedResult, HttpStatus.OK));

        OpenWeatherDataDto actualResult = openWeatherService.getWeather(city, country);
        assertTrue(new ReflectionEquals(expectedResult).matches(actualResult));
    }

    @Test
    public void shouldThrowNotFoundException() {
        String city = "Shanghai";
        String country = "China";
        String requestUrl = String.format("%s?q=%s,%s&appid=%s&units=%s", API_URL, city, country, API_KEY, UNITS_VALUE);

        Mockito.when(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.GET), any(), eq(OpenWeatherDataDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "City not found"));

        CityNotFoundException cityNotFoundException = null;
        try {
            openWeatherService.getWeather(city, country);
        } catch (Exception ex) {
            cityNotFoundException = ex instanceof CityNotFoundException ? (CityNotFoundException) ex : null;
        }

        assertNotNull(cityNotFoundException);
        assertEquals("Could not find city Shanghai", cityNotFoundException.getMessage());
    }

    @Test
    public void shouldThrowOpenWeatherApiException() {
        String city = "Sydney";
        String country = "Australia";
        String requestUrl = String.format("%s?q=%s,%s&appid=%s&units=%s", API_URL, city, country, API_KEY, UNITS_VALUE);

        Mockito.when(restTemplate.exchange(eq(requestUrl), eq(HttpMethod.GET), any(), eq(OpenWeatherDataDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE, "System error"));

        OpenWeatherApiException openWeatherApiException = null;
        try {
            openWeatherService.getWeather(city, country);
        } catch (Exception ex) {
            openWeatherApiException = ex instanceof OpenWeatherApiException ? (OpenWeatherApiException) ex : null;
        }

        assertNotNull(openWeatherApiException);
        assertEquals("503 System error", openWeatherApiException.getMessage());
    }

}
