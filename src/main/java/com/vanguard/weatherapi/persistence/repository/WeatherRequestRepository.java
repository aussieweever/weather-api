package com.vanguard.weatherapi.persistence.repository;

import com.vanguard.weatherapi.persistence.entity.User;
import com.vanguard.weatherapi.persistence.entity.WeatherRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface WeatherRequestRepository extends JpaRepository<WeatherRequest, Long> {
    long countWeatherRequestByUserIsAndRequestedOnAfter(User user, OffsetDateTime requestedOn);
}
