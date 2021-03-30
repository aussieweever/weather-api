package com.vanguard.weatherapi.controller;

import com.vanguard.weatherapi.aspect.RateLimitAspect;
import com.vanguard.weatherapi.dto.WeatherDto;
import com.vanguard.weatherapi.exception.CityNotFoundException;
import com.vanguard.weatherapi.persistence.entity.User;
import com.vanguard.weatherapi.persistence.repository.UserRepository;
import com.vanguard.weatherapi.persistence.repository.WeatherRequestRepository;
import com.vanguard.weatherapi.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WeatherControllerTest {
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private WeatherRequestRepository weatherRequestRepository;

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
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(weatherController)
                .setControllerAdvice(new AppExceptionHandler())
                .build();
    }

    @Test
    public void shouldGetWeather() throws Exception {
        String city = "Melbourne";
        String country = "Australia";
        String apiKey = "fakeApiKey";
        String description = "Clear Sky";

        WeatherDto weatherDto = WeatherDto.builder()
                .city(city)
                .country(country)
                .description(description)
                .build();
        User user = User.builder().apiKey(apiKey).enabled(true).build();
        Mockito.when(userRepository.getTopByApiKeyAndEnabledIsTrue(eq(apiKey)))
                .thenReturn(user);
        Mockito.when(weatherRequestRepository.countWeatherRequestByUserIsAndRequestedOnAfter(eq(user), any(OffsetDateTime.class)))
                .thenReturn(4L);
        Mockito.when(weatherService.getWeather(eq(city), eq(country))).thenReturn(weatherDto);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/weather")
                        .param("city", city)
                        .param("country", country)
                        .header("X-API-Key", apiKey)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.city").value(city))
                .andExpect(MockMvcResultMatchers.jsonPath("$.country").value(country))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(description));
    }

    @Test
    public void shouldReturn401UnAuthorized() throws Exception {
        String city = "Melbourne";
        String country = "Australia";
        String apiKey = "fakeApiKey";

        Mockito.when(userRepository.getTopByApiKeyAndEnabledIsTrue(eq(apiKey)))
                .thenReturn(null);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/weather")
                        .param("city", city)
                        .param("country", country)
                        .header("X-API-Key", apiKey)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value("Invalid API Key"));
    }

    @Test
    public void shouldReturn404NotFound() throws Exception {
        String city = "Melbourne";
        String country = "Australia";
        String apiKey = "fakeApiKey";
        String errorMessage = "Could not find city Melbourne";


        User user = User.builder().apiKey(apiKey).enabled(true).build();
        Mockito.when(userRepository.getTopByApiKeyAndEnabledIsTrue(eq(apiKey)))
                .thenReturn(user);
        Mockito.when(weatherRequestRepository.countWeatherRequestByUserIsAndRequestedOnAfter(eq(user), any(OffsetDateTime.class)))
                .thenReturn(4L);
        Mockito.when(weatherService.getWeather(eq(city), eq(country))).thenThrow(new CityNotFoundException(errorMessage));


        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/weather")
                        .param("city", city)
                        .param("country", country)
                        .header("X-API-Key", apiKey)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(errorMessage));
    }

    @Test
    public void shouldReturn429TooManyRequests() throws Exception {
        String city = "Melbourne";
        String country = "Australia";
        String apiKey = "fakeApiKey";
        String errorMessage = "Maximum 5 requests per 2 minutes";

        User user = User.builder().apiKey(apiKey).enabled(true).build();
        Mockito.when(userRepository.getTopByApiKeyAndEnabledIsTrue(eq(apiKey)))
                .thenReturn(user);
        Mockito.when(weatherRequestRepository.countWeatherRequestByUserIsAndRequestedOnAfter(eq(user), any(OffsetDateTime.class)))
                .thenReturn(5L);
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/weather")
                        .param("city", city)
                        .param("country", country)
                        .header("X-API-Key", apiKey)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isTooManyRequests())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(errorMessage));
    }

    @Test
    public void shouldReturn500InternalError() throws Exception {
        String city = "Melbourne";
        String country = "Australia";
        String apiKey = "fakeApiKey";
        String errorMessage = "Unknown error";

        User user = User.builder().apiKey(apiKey).enabled(true).build();
        Mockito.when(userRepository.getTopByApiKeyAndEnabledIsTrue(eq(apiKey)))
                .thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/weather")
                        .param("city", city)
                        .param("country", country)
                        .header("X-API-Key", apiKey)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value(errorMessage));
    }
}
