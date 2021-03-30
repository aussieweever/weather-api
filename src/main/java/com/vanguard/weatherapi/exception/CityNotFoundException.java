package com.vanguard.weatherapi.exception;

public class CityNotFoundException extends RuntimeException {
    public CityNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
