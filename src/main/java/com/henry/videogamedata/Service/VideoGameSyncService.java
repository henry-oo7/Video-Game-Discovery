package com.henry.videogamedata.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.henry.videogamedata.Entity.Genre;
import com.henry.videogamedata.Entity.Platform;
import com.henry.videogamedata.Entity.VideoGame;
import com.henry.videogamedata.Repository.GenreRepository;
import com.henry.videogamedata.Repository.PlatformRepository;
import com.henry.videogamedata.api.IGDBClient;
import com.henry.videogamedata.Repository.VideoGameRepository;
import com.henry.videogamedata.constant.PlatformConstants;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;


@Service // 1. This tells Spring: "Load this class on startup!"
public class VideoGameSyncService {

    // 2. 'private final' ensures these can't be changed once set
    private final IGDBClient igdbClient;
    private final VideoGameRepository videoGameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;

    // 3. The Constructor
    // Spring sees this and automatically injects the dependencies
    public VideoGameSyncService(IGDBClient igdbClient,
                                VideoGameRepository videoGameRepository,
                                GenreRepository genreRepository,
                                PlatformRepository platformRepository) {
        this.igdbClient = igdbClient;
        this.videoGameRepository = videoGameRepository;
        this.genreRepository = genreRepository;
        this.platformRepository = platformRepository;
    }


    //enable scheduling to allow to refreash games list every 24 hours
    @Scheduled(fixedRate = 86_400_000)
    public void syncGames(){
        HashMap<String, Genre> genreMap = new HashMap<>();
        HashMap<String, Platform> platformMap = new HashMap<>();

        Genre noneGenre = new Genre();
        noneGenre.setGenreName("none");
        getOrCreateGenre(noneGenre, genreMap);

        long maxUpdatedAt = videoGameRepository.findMaxUpdatedAt().orElse(0L);
        while(true) {
            try {
                List<VideoGame> gamesList = igdbClient.fetchGamesBatch(maxUpdatedAt);
                List<VideoGame> gamesToSave = new ArrayList<>();
                if(gamesList.isEmpty()){
                    break;
                }
                else {
                    long startTime = System.currentTimeMillis();
                    for (VideoGame videoGame : gamesList) {
                        ArrayList<Genre> genrelist = new ArrayList<>();
                        ArrayList<Platform> platformList = new ArrayList<>();

                        if (videoGame.getPlatforms() == null) {
                            continue;
                        }

                        boolean hasAllowedPlatform = videoGame.getPlatforms().stream().anyMatch(platform -> PlatformConstants.ALLOWED_PLATFORMS.contains(platform.getPlatformName()));

                        if (!hasAllowedPlatform) {
                            continue;
                        }
                        for (Platform platform: videoGame.getPlatforms()) {
                            if (!PlatformConstants.ALLOWED_PLATFORMS.contains(platform.getPlatformName())) {
                                continue;
                            }
                            platformList.add(getOrCreatePlatform(platform, platformMap));
                        }

                        if (videoGame.getGenres() == null) {
                            genrelist.add(genreMap.get("none"));
                        }
                        else {
                            for (Genre genre : videoGame.getGenres()) {
                                genrelist.add(getOrCreateGenre(genre, genreMap));
                            }
                        }
                        videoGame.setGenres(genrelist);
                        videoGame.setPlatforms(platformList);
                        gamesToSave.add(videoGame);
                    }
                    videoGameRepository.saveAll(gamesToSave);
                    long endtime = System.currentTimeMillis();
                    long duration = endtime - startTime;
                    VideoGame lastGame = gamesList.getLast();
                    maxUpdatedAt = lastGame.getUpdatedAt();
                    System.out.println("Successfully synced " + gamesToSave.size() + " games");
                    System.out.println("Time taken: " + duration + " ms");
                    System.out.println("Synced batch ending at: " + maxUpdatedAt);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //helper method to speed up syncing process as to not check database every single time a genre or platform needs to be checked
    private Platform getOrCreatePlatform(Platform incomingPlatform, Map<String, Platform> cache) {
        if (cache.containsKey(incomingPlatform.getPlatformName())) {
            return cache.get(incomingPlatform.getPlatformName());
        }
        Optional<Platform> existing = platformRepository.findByPlatformName(incomingPlatform.getPlatformName());

        if (existing.isPresent()) {
            Platform dbPlatform = existing.get();

            cache.put(dbPlatform.getPlatformName(), dbPlatform);
            return dbPlatform;
        } else {

            Platform savedPlatform = platformRepository.save(incomingPlatform);
            cache.put(savedPlatform.getPlatformName(), savedPlatform);
            return savedPlatform;
        }
    }

    private Genre getOrCreateGenre(Genre incomingGenre, Map<String, Genre> cache) {

        if (cache.containsKey(incomingGenre.getGenreName())) {
            return cache.get(incomingGenre.getGenreName());
        }

        Optional<Genre> existing = genreRepository.findByGenreName(incomingGenre.getGenreName());

        if (existing.isPresent()) {
            Genre dbGenre = existing.get();

            cache.put(dbGenre.getGenreName(), dbGenre);
            return dbGenre;
        } else {
            Genre savedGenre = genreRepository.save(incomingGenre);
            cache.put(savedGenre.getGenreName(), savedGenre);
            return savedGenre;
        }
    }
}