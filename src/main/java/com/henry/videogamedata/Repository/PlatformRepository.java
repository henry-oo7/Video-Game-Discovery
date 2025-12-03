package com.henry.videogamedata.Repository;

import com.henry.videogamedata.Entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformRepository extends JpaRepository<Platform,Integer> {

    Optional<Platform> findByPlatformName(String platformName);
}
