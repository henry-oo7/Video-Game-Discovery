package com.henry.videogamedata.Entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "video_game", indexes = {
        @Index(name = "idx_video_game_name", columnList = "name")
})
public class VideoGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;

    @JsonAlias("id")
    private long igdbId;

    private String name;

    private String coverUrl;

    @JsonProperty("cover")
    public void unpackCover(Map<String, Object> cover) {
        if (cover != null && cover.get("url") != null) {

            String url = cover.get("url").toString();

            url = url.replace("t_thumb","t_1080p");

            this.coverUrl = "https:" + url;
        }
    }

    @ManyToMany
    @JoinTable(
            name = "game_genres",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres;

    @ManyToMany
    @JoinTable(
            name = "game_platforms",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "platform_id")
    )
    private List<Platform> platforms;

    @JsonAlias("updated_at")
    private long updatedAt;

    @JsonAlias("total_rating")
    private Double totalRating;

    @JsonAlias("total_rating_count")
    private Integer totalRatingCount;

    @JsonAlias("first_release_date")
    private Long firstReleaseDate;

}
