package com.henry.videogamedata.Controller;

import com.henry.videogamedata.Entity.User;
import com.henry.videogamedata.Entity.VideoGame;
import com.henry.videogamedata.Service.RecommendationService;
import com.henry.videogamedata.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    UserService userService;
    RecommendationService recommendationService;

    UserController(UserService userService, RecommendationService recommendationService) {
        this.userService = userService;
        this.recommendationService = recommendationService;
    }

    //
    @PostMapping("/favorites")
    public ResponseEntity<String> saveFavorites(@RequestBody User.FavoriteRequest favoriteRequest){
        User user = userService.saveUserFavorites(favoriteRequest);
        if (user == null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().body("success");
    }

    @GetMapping("/has-voted")
    public ResponseEntity<Boolean> checkVoteStatus(@RequestParam String visitorId) {
        boolean hasVoted = userService.hasVoted(visitorId);
        return ResponseEntity.ok().body(hasVoted);
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<VideoGame>> getUserFavorites(@RequestParam String visitorId){
        return ResponseEntity.ok().body(userService.getUserFavorites(visitorId));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<VideoGame>> getRecommendations(@RequestParam String visitorId) {
        List<VideoGame> recs = recommendationService.getRecommendedVideoGameList(visitorId);
        return ResponseEntity.ok(recs);
    }
}
