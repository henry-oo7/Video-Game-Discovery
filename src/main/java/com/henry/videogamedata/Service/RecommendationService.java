package com.henry.videogamedata.Service;

import com.henry.videogamedata.Entity.Genre;
import com.henry.videogamedata.Entity.Platform;
import com.henry.videogamedata.Entity.User;
import com.henry.videogamedata.Entity.VideoGame;
import com.henry.videogamedata.Repository.UserRepository;
import com.henry.videogamedata.Repository.VideoGameRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final UserRepository userRepository;
    private final VideoGameRepository videoGameRepository;

    RecommendationService(UserRepository userRepository, VideoGameRepository videoGameRepository) {
        this.userRepository = userRepository;
        this.videoGameRepository = videoGameRepository;
    }

    public List<VideoGame> getRecommendedVideoGameList(String visitorId) {
        User user = userRepository.findUserByUserIdentifier(visitorId).orElse(null);
        if (user == null || user.getFavoriteGames().isEmpty()) {
            return new ArrayList<>();
        }

        List<VideoGame> favoritesGameList = user.getFavoriteGames();


        Map<Integer, Integer> genreWeights = new HashMap<>();
        Map<Integer, Integer> platformWeights = new HashMap<>();
        Set<Genre> searchGenres = new HashSet<>();
        List<Integer> excludedIds = new ArrayList<>();

        for (VideoGame fav : favoritesGameList) {
            excludedIds.add(fav.getId());


            for (Genre g : fav.getGenres()) {
                genreWeights.put(g.getId(), genreWeights.getOrDefault(g.getId(), 0) + 1);
                searchGenres.add(g);
            }

            for (Platform p : fav.getPlatforms()) {
                platformWeights.put(p.getId(), platformWeights.getOrDefault(p.getId(), 0) + 1);
            }
        }


        List<VideoGame> candidates = videoGameRepository.findRecommendationCandidates(
                new ArrayList<>(searchGenres),
                excludedIds,
                PageRequest.of(0, 100)
        );


        Map<VideoGame, Double> scoredCandidates = new HashMap<>();

        for (VideoGame game : candidates) {
            double score = 0.0;


            for (Genre g : game.getGenres()) {
                int frequency = genreWeights.getOrDefault(g.getId(), 0);
                if (frequency > 0) {
                    score += (frequency * 3.0);
                }
            }


            for (Platform p : game.getPlatforms()) {
                int frequency = platformWeights.getOrDefault(p.getId(), 0);
                if (frequency > 0) {
                    score += (frequency * 1.0);
                }
            }


            if (game.getTotalRating() != null) {
                score += (game.getTotalRating() / 10.0);
            }

            scoredCandidates.put(game, score);
        }


        return scoredCandidates.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // Sort High to Low
                .limit(5) // Take Top 5
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}