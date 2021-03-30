package com.vanguard.weatherapi.exception;

public class TooManyRequestException extends RuntimeException {
    public TooManyRequestException(String errorMessage) {
        super(errorMessage);
    }
}
