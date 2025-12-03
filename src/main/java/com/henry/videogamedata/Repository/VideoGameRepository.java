package com.henry.videogamedata.Repository;

import com.henry.videogamedata.Entity.Genre;
import com.henry.videogamedata.Entity.VideoGame;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VideoGameRepository extends JpaRepository<VideoGame,Integer> {

    @Query("select max (v.updatedAt) from VideoGame v")
    Optional<Long> findMaxUpdatedAt();

    @Query("SELECT v FROM VideoGame v " +
            "WHERE v.id IN (SELECT MIN(v2.id) FROM VideoGame v2 GROUP BY v2.name) " +
            "ORDER BY " +
            "  (CASE WHEN v.firstReleaseDate >= :cutoffDate THEN 1 ELSE 0 END) DESC, " +
            "  v.totalRatingCount DESC, " +
            "  v.totalRating DESC")
    Page<VideoGame> findTrendingGames(long cutoffDate, Pageable pageable);


    @Query(value = "SELECT * FROM video_game v " +
            "WHERE (:term IS NULL OR MATCH(v.name) AGAINST(:term IN BOOLEAN MODE)) " +
            "AND (:genre IS NULL OR v.id IN (SELECT game_id FROM game_genres WHERE genre_id = (SELECT id FROM genre WHERE genre_name = :genre))) " +
            "AND (:platform IS NULL OR v.id IN (SELECT game_id FROM game_platforms WHERE platform_id = (SELECT id FROM platform WHERE platform_name = :platform))) " +
            "ORDER BY (" +
            "  (COALESCE(v.total_rating, 0) * 200000000) + " +
            "  COALESCE(v.first_release_date, 0) + " +
            "  (COALESCE(v.total_rating_count, 0) * 1000)" +
            ") DESC",
            countQuery = "SELECT count(*) FROM video_game v " +
                    "WHERE (:term IS NULL OR MATCH(v.name) AGAINST(:term IN BOOLEAN MODE)) " +
                    "AND (:genre IS NULL OR v.id IN (SELECT game_id FROM game_genres WHERE genre_id = (SELECT id FROM genre WHERE genre_name = :genre))) " +
                    "AND (:platform IS NULL OR v.id IN (SELECT game_id FROM game_platforms WHERE platform_id = (SELECT id FROM platform WHERE platform_name = :platform)))",
            nativeQuery = true)
    Page<VideoGame> searchGamesNative(@Param("term") String term,
                                      @Param("genre") String genre,
                                      @Param("platform") String platform,
                                      Pageable pageable);

    @Query("SELECT DISTINCT v FROM VideoGame v " +
            "JOIN v.genres g " +
            "WHERE g IN :genres " +
            "AND v.id NOT IN :excludedIds " +
            "AND v.id IN (SELECT MIN(v2.id) FROM VideoGame v2 GROUP BY v2.name) " + // <--- THE FILTER 🧹
            "ORDER BY v.totalRatingCount DESC")
    List<VideoGame> findRecommendationCandidates(
            @Param("genres") List<Genre> genres,
            @Param("excludedIds") List<Integer> excludedIds,
            Pageable pageable
    );

}
