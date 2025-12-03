package com.henry.videogamedata.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user") // "user" is a reserved word in SQL!
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String userIdentifier;

    @ManyToMany
    @JoinTable(
            name = "user_favorites",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    private List<VideoGame> favoriteGames;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteRequest {
        private String visitorId;
        private List<Integer> gameIds;
        private String name;
    }

}
