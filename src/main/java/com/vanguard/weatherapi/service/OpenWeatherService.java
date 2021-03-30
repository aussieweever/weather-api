package com.vanguard.weatherapi.service;

import com.vanguard.weatherapi.dto.openweather.OpenWeatherDataDto;
import com.vanguard.weatherapi.exception.CityNotFoundException;
import com.vanguard.weatherapi.exception.OpenWeatherApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;


@Service
@Slf4j
@RequiredArgsConstructor
public class OpenWeatherService {
    private final RestTemplate restTemplate;
    @Value("${open-weather-api.url}")
    private String apiUrl;
    @Value("${open-weather-api.api-key}")
    private String apiKey;
    @Value("${open-weather-api.units}")
    private String units;

    public OpenWeatherDataDto getWeather(String city, String country) {
        log.info("Retrieving weather data for city {} of country {} from OpenWeatherMap", city, country);
        try {
            String requestUrl = UriComponentsBuilder.fromHttpUrl(this.apiUrl)
                    .queryParam("q", String.join(",", city, country))
                    .queryParam("appid", apiKey)
                    .queryParam("units", units)
                    .toUriString();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(httpHeaders);

            ResponseEntity<OpenWeatherDataDto> response = restTemplate
                    .exchange(requestUrl, HttpMethod.GET, request, OpenWeatherDataDto.class);

            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException httpError) {
            log.error("Could not get weather data", httpError);
            if (HttpStatus.NOT_FOUND.equals(httpError.getStatusCode())) {
                throw new CityNotFoundException("Could not find city " + city);
            }

            throw new OpenWeatherApiException(httpError.getMessage());
        }
    }
}
