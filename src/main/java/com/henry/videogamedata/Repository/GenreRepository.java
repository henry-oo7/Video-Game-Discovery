package com.henry.videogamedata.Repository;

import com.henry.videogamedata.Entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre,Integer> {

    Optional<Genre> findByGenreName(String genreName);
}
