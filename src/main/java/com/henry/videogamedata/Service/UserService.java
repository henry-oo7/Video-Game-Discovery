package com.henry.videogamedata.Service;

import com.henry.videogamedata.Entity.User;
import com.henry.videogamedata.Entity.VideoGame;
import com.henry.videogamedata.Repository.UserRepository;
import com.henry.videogamedata.Repository.VideoGameRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    UserRepository userRepository;
    VideoGameRepository videoGameRepository;

    UserService(UserRepository userRepository,VideoGameRepository videoGameRepository) {
        this.userRepository = userRepository;
        this.videoGameRepository = videoGameRepository;
    }


    //helper method to create or get user based on if they are in the database or not
    public User getOrCreateUser(String userIdentifier) {
        User user = userRepository.findUserByUserIdentifier(userIdentifier).orElse(null);
        if  (user == null) {
            user = new User();
            user.setUserIdentifier(userIdentifier);
            userRepository.save(user);
        }
        return user;
    }

    //saves user favorites after choosing and clicking submit
    public User saveUserFavorites(User.FavoriteRequest favoriteRequest) {
        User favoriteUser = getOrCreateUser(favoriteRequest.getVisitorId());
        List<VideoGame> videoGame = videoGameRepository.findAllById(favoriteRequest.getGameIds());
        favoriteUser.setFavoriteGames(videoGame);
        favoriteUser.setName(favoriteRequest.getName());
        return userRepository.save(favoriteUser);
    }

    //helper method to check if unique visitorId has voted so stop them from continuing to submit
    public boolean hasVoted(String visitorId) {
        User user = userRepository.findUserByUserIdentifier(visitorId).orElse(null);

        return user != null;
    }


    //helper method to retrieve user favorites to run through recommendation algo
    public List<VideoGame> getUserFavorites(String visitorId) {
        User user = userRepository.findUserByUserIdentifier(visitorId).orElse(null);
        if (user != null) {
            return user.getFavoriteGames();
        }
        return List.of();
    }

}
