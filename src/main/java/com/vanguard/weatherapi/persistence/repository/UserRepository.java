package com.vanguard.weatherapi.persistence.repository;

import com.vanguard.weatherapi.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User getTopByApiKeyAndEnabledIsTrue(String apiKey);
}
