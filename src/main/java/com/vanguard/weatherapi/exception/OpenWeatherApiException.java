package com.vanguard.weatherapi.exception;

public class OpenWeatherApiException extends RuntimeException {
    public OpenWeatherApiException(String errorMessage) {
        super(errorMessage);
    }
}
