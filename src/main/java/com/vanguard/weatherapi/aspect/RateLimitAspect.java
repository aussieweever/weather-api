package com.vanguard.weatherapi.aspect;

import com.vanguard.weatherapi.exception.InvalidApiKeyException;
import com.vanguard.weatherapi.exception.TooManyRequestException;
import com.vanguard.weatherapi.persistence.entity.User;
import com.vanguard.weatherapi.persistence.entity.WeatherRequest;
import com.vanguard.weatherapi.persistence.repository.UserRepository;
import com.vanguard.weatherapi.persistence.repository.WeatherRequestRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component
@Scope
@Aspect
@RequiredArgsConstructor
public class RateLimitAspect {
    private static final String API_KEY_HEADER = "X-API-Key";
    private final UserRepository userRepository;
    private final WeatherRequestRepository weatherRequestRepository;

    @Value("${app.rate-limit-duration-minutes}")
    private int rateLimitDurationMinutes;

    @Value("${app.rate-limit-max-requests}")
    private int rateLimitMaxRequests;

    @Pointcut("@annotation(com.vanguard.weatherapi.annotation.RateLimit)")
    public void rateLimitAspect() {
    }

    @Around("rateLimitAspect()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        String apiKey = request.getHeader(API_KEY_HEADER);
        User user = userRepository.getTopByApiKeyAndEnabledIsTrue(apiKey);

        if (user == null) {
            throw new InvalidApiKeyException("Invalid API Key");
        }

        weatherRequestRepository.save(WeatherRequest.builder()
                .user(user)
                .requestedOn(OffsetDateTime.now())
                .build());

        OffsetDateTime limitStartTime = OffsetDateTime.now().minus(rateLimitDurationMinutes, ChronoUnit.MINUTES);
        long totalRequested = weatherRequestRepository.countWeatherRequestByUserIsAndRequestedOnAfter(user, limitStartTime);
        if (totalRequested >= rateLimitMaxRequests) {
            throw new TooManyRequestException(String.format("Maximum %d requests per %d minutes", rateLimitMaxRequests, rateLimitDurationMinutes));
        }
        return joinPoint.proceed();
    }
}
