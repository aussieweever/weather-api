package com.vanguard.weatherapi.controller;

import com.vanguard.weatherapi.dto.ErrorDto;
import com.vanguard.weatherapi.exception.CityNotFoundException;
import com.vanguard.weatherapi.exception.InvalidApiKeyException;
import com.vanguard.weatherapi.exception.OpenWeatherApiException;
import com.vanguard.weatherapi.exception.TooManyRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<ErrorDto> processCityNotFoundException(CityNotFoundException exception) {
        return new ResponseEntity<ErrorDto>(new ErrorDto(exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<ErrorDto> processTooManyRequestsException(TooManyRequestException exception) {
        return new ResponseEntity<ErrorDto>(new ErrorDto(exception.getMessage()), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler({OpenWeatherApiException.class, Exception.class})
    public ResponseEntity<ErrorDto> processOtherExceptions(Exception exception) {
        return new ResponseEntity<ErrorDto>(new ErrorDto(exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<ErrorDto> processInvalidApiKeyException(InvalidApiKeyException exception) {
        return new ResponseEntity<ErrorDto>(new ErrorDto(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }
}
