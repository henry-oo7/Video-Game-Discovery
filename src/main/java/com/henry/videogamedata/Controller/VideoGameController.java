package com.henry.videogamedata.Controller;

import com.henry.videogamedata.Entity.Genre;
import com.henry.videogamedata.Entity.Platform;
import com.henry.videogamedata.Entity.VideoGame;
import com.henry.videogamedata.Service.RecommendationService;
import com.henry.videogamedata.Service.VideoGameService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/videogames")
@CrossOrigin(origins = "*")
public class VideoGameController {
    VideoGameService videoGameService;
    VideoGameController(VideoGameService videoGameService) {
        this.videoGameService = videoGameService;
    }

    @GetMapping()
    public Page<VideoGame> getVideoGames(@RequestParam Optional<String> startsWith,
                                         @RequestParam Optional<String> genre,
                                         @RequestParam Optional<String> platform,
                                         @RequestParam(defaultValue = "0") Integer page,
                                         @RequestParam(defaultValue = "20") Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return videoGameService.getGames(startsWith.orElse(null), genre.orElse(null), platform.orElse(null), pageable);
    }

    @GetMapping("/genres")
    public List<Genre> getAllGenres(){
        return videoGameService.getAllGenres();
    }

    @GetMapping("/platforms")
    public List<Platform> getAllPlatforms(){
        return videoGameService.getAllPlatforms();
    }


}
