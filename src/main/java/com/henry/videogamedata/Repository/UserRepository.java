package com.henry.videogamedata.Repository;

import com.henry.videogamedata.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findUserByUserIdentifier(String userIdentifier);
}
