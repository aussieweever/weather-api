package com.vanguard.weatherapi.persistence.repository;

import com.vanguard.weatherapi.persistence.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Weather findTopByCityAndCountryOrderByUpdatedOnDesc(String city, String country);
}
