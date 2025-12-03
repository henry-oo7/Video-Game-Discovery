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


    //generates a reccomended games list based on a combination of matching genres, platforms, and bonus points for if the game is rated highly
    public List<VideoGame> getRecommendedVideoGameList(String visitorId) {
        User user = userRepository.findUserByUserIdentifier(visitorId).orElse(null);
        if (user == null || user.getFavoriteGames().isEmpty()) {
            return new ArrayList<>();
        }

        List<VideoGame> favoritesGameList = user.getFavoriteGames();

        Set<Genre> likedGenres = new HashSet<>();
        Set<Platform> likedPlatforms = new HashSet<>();
        List<Integer> excludedIds = new ArrayList<>();

        for (VideoGame videoGame : favoritesGameList) {
            likedGenres.addAll(videoGame.getGenres());
            likedPlatforms.addAll(videoGame.getPlatforms());
            excludedIds.add(videoGame.getId());
        }

        List<VideoGame> recommendedVideoGames = videoGameRepository.findRecommendationCandidates(new ArrayList<>(likedGenres),excludedIds, PageRequest.of(0,50));

        Map<VideoGame, Double> scores = new HashMap<>();

        for (VideoGame videoGame : recommendedVideoGames) {
            double score = 0.0;

            for (Genre genre : likedGenres) {
                if (likedGenres.contains(genre)) {
                    score+=2;
                }
            }
            for (Platform platform : likedPlatforms) {
                if (likedPlatforms.contains(platform)) {
                    score+=1;
                }
            }
            if (videoGame.getTotalRating() != null) {
                score += (videoGame.getTotalRating() / 100.0);
            }
            scores.put(videoGame, score);
        }

        return scores.entrySet().stream()
                .sorted((a,b) -> Double.compare(b.getValue(),a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
