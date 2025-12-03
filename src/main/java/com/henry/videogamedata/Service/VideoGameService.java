package com.henry.videogamedata.Service;

import com.henry.videogamedata.Entity.Genre;
import com.henry.videogamedata.Entity.Platform;
import com.henry.videogamedata.Entity.VideoGame;
import com.henry.videogamedata.Repository.GenreRepository;
import com.henry.videogamedata.Repository.PlatformRepository;
import com.henry.videogamedata.Repository.VideoGameRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
public class VideoGameService {
    private final VideoGameRepository videoGameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;

    VideoGameService(VideoGameRepository videoGameRepository, GenreRepository genreRepository, PlatformRepository platformRepository) {
        this.videoGameRepository = videoGameRepository;
        this.genreRepository = genreRepository;
        this.platformRepository = platformRepository;
    }

    //Common words that MySQL ignores by default
    private static final Set<String> STOPWORDS = Set.of(
            "the", "of", "and", "a", "an", "in", "to", "for", "on", "at", "by"
    );

    //String used to format searchterm in a way that makes it easy to use for my sql queries
    private String formatSearchTerm(String input) {
        if (input == null || input.isEmpty()) return "";

        String clean = input.replaceAll("[^a-zA-Z0-9\\s]", " ");

        String[] words = clean.trim().split("\\s+");

        StringBuilder query = new StringBuilder();
        for (String word : words) {

            if (word.isEmpty()) continue;

            if (STOPWORDS.contains(word.toLowerCase())) {
                continue;
            }

            query.append("+").append(word).append("* ");
        }

        return query.toString().trim();
    }

    //the big get games method
    //added a cache to speed up resetting queries and going back to the main games
    @Cacheable(value = "landingPage", key = "'defaultGames'", condition = "#name == null && #genre == null && #platform == null && #pageable.pageNumber == 0")
    public Page<VideoGame> getGames(String name, String genre, String platform, Pageable pageable) {

        boolean hasFilters = (genre != null && !genre.isEmpty()) || (platform != null && !platform.isEmpty());

        //Landing Page Logic (No Search, No Filters) (cached)
        if (!hasFilters && (name == null || name.isEmpty())) {
            long currentYearTimestamp = LocalDate.now().withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            return videoGameRepository.findTrendingGames(currentYearTimestamp, pageable);
        }

        //Search Logic (With or Without Filters)
        String formattedSearch = null;
        if (name != null && !name.isEmpty()) {
            formattedSearch = formatSearchTerm(name);
            if (formattedSearch.isEmpty()) {
                // If user typed only stopwords/garbage, ignore the name filter but keep other filters
                formattedSearch = null;
            }
        }

        //Unified Call -- if formattedSearch is null, sql will be able to handle it gracefull
        return videoGameRepository.searchGamesNative(formattedSearch, genre, platform, pageable);
    }


    public List<Genre> getAllGenres(){
        return genreRepository.findAll();
    }

    public List<Platform> getAllPlatforms(){
        return platformRepository.findAll();
    }
}
