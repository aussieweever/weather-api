package com.vanguard.weatherapi.exception;

public class InvalidApiKeyException extends RuntimeException {
    public InvalidApiKeyException(String errorMessage) {
        super(errorMessage);
    }
}
