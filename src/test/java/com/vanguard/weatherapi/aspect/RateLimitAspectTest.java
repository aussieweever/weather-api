package com.vanguard.weatherapi.aspect;

import com.vanguard.weatherapi.controller.WeatherController;
import com.vanguard.weatherapi.dto.WeatherDto;
import com.vanguard.weatherapi.exception.InvalidApiKeyException;
import com.vanguard.weatherapi.exception.TooManyRequestException;
import com.vanguard.weatherapi.persistence.entity.User;
import com.vanguard.weatherapi.persistence.repository.UserRepository;
import com.vanguard.weatherapi.persistence.repository.WeatherRequestRepository;
import com.vanguard.weatherapi.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
public class RateLimitAspectTest {
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private WeatherRequestRepository weatherRequestRepository;
    @MockBean
    private WeatherService weatherService;

    private RateLimitAspect rateLimitAspect;
    private WeatherController weatherController;

    @BeforeEach
    public void setup() {
        WeatherController target = new WeatherController(weatherService);
        AspectJProxyFactory factory = new AspectJProxyFactory(target);

        rateLimitAspect = new RateLimitAspect(userRepository, weatherRequestRepository);
        ReflectionTestUtils.setField(rateLimitAspect, "rateLimitDurationMinutes", 2);
        ReflectionTestUtils.setField(rateLimitAspect, "rateLimitMaxRequests", 5);
        factory.addAspect(rateLimitAspect);

        weatherController = factory.getProxy();
    }

    @Test
    public void shouldGetWeather() {
        String apiKey = "123456789";
        String city = "Melbourne";
        String country = "Australia";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", apiKey);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        User expectedUser = User.builder()
                .apiKey(apiKey)
                .enabled(true)
                .build();

        Mockito.when(userRepository.getTopByApiKeyAndEnabledIsTrue(eq(apiKey))).thenReturn(expectedUser);
        Mockito.when(weatherRequestRepository
                .countWeatherRequestByUserIsAndRequestedOnAfter(eq(expectedUser), any(OffsetDateTime.class)))
                .thenReturn(4L);
        WeatherDto weatherDto = WeatherDto.builder()
                .city(city)
                .country(country)
                .description("Sunny")
                .build();
        Mockito.when(weatherService.getWeather(eq(city), eq(country))).thenReturn(weatherDto);
        WeatherDto actualResult = weatherController.getWeather(city, country);
        assertTrue(new ReflectionEquals(weatherDto).matches(actualResult));
    }

    @Test
    public void shouldThrowInvalidApiKeyException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        InvalidApiKeyException expectedException = null;
        try {
            weatherController.getWeather("Sydney", "Australia");
        } catch (Exception ex) {
            expectedException = ex instanceof InvalidApiKeyException ? (InvalidApiKeyException) ex : null;
        }

        assertNotNull(expectedException);
    }

    @Test
    public void shouldThrowTooManyRequestsException() {
        String apiKey = "123456789";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", apiKey);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        User expectedUser = User.builder()
                .apiKey(apiKey)
                .enabled(true)
                .build();

        Mockito.when(userRepository.getTopByApiKeyAndEnabledIsTrue(eq(apiKey))).thenReturn(expectedUser);
        Mockito.when(weatherRequestRepository
                .countWeatherRequestByUserIsAndRequestedOnAfter(eq(expectedUser), any(OffsetDateTime.class)))
                .thenReturn(5L);

        TooManyRequestException expectedException = null;
        try {
            weatherController.getWeather("Sydney", "Australia");
        } catch (Exception ex) {
            expectedException = ex instanceof TooManyRequestException ? (TooManyRequestException) ex : null;
        }

        assertNotNull(expectedException);
    }
}
