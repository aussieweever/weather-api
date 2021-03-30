package com.vanguard.weatherapi.controller;

import com.sun.istack.NotNull;
import com.vanguard.weatherapi.annotation.RateLimit;
import com.vanguard.weatherapi.dto.ErrorDto;
import com.vanguard.weatherapi.dto.WeatherDto;
import com.vanguard.weatherapi.service.WeatherService;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.WordUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    @RateLimit
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "Successful response",
                    response = WeatherDto.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized",
                    response = ErrorDto.class),
            @ApiResponse(
                    code = 404,
                    message = "City not found",
                    response = ErrorDto.class
            ),
            @ApiResponse(
                    code = 429,
                    message = "Too many requests",
                    response = ErrorDto.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "Server internal error",
                    response = ErrorDto.class
            )
    })
    public WeatherDto getWeather(@RequestParam("city") @ApiParam(value = "The name of the city. eg. Melbourne", name = "city") @NotNull String city,
                                 @RequestParam("country") @ApiParam(value = " The full name of the country. eg. Australia", name = "country") @NotNull String country) {
        return weatherService.getWeather(WordUtils.capitalize(city), WordUtils.capitalize(country));
    }
}
