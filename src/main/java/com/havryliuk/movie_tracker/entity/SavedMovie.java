package com.havryliuk.movie_tracker.entity;

import com.havryliuk.movie_tracker.dto.TmdbMovieDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SavedMovie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tmdbId;

    private String title;

    @Column(length = 1000)
    private String overview;

    private String posterUrl;

    private Double tmdbRating;

    private Double userRating;

    // Перевизначаємо геттер Lombok для збережених фільмів
    public Double getTmdbRating() {
        if (this.tmdbRating == null) return null;
        return Math.round(this.tmdbRating * 10.0) / 10.0;
    }

    public void fillFromApi(TmdbMovieDTO apiMovie) {
        this.title = apiMovie.getTitle();
        this.tmdbRating = apiMovie.getRating();

        String overview = apiMovie.getOverview();
        this.overview = (overview != null && overview.length() > 950) ? overview.substring(0, 950) + "..." : overview;

        this.posterUrl = (apiMovie.getPosterPath() != null && !apiMovie.getPosterPath().isEmpty())
                ? "https://image.tmdb.org/t/p/w500" + apiMovie.getPosterPath()
                : "https://via.placeholder.com/500x750?text=No+Poster";
    }

    // Зв'язок Many-to-One (Багато фільмів належать до однієї колекції)
    @ManyToOne
    @JoinColumn(name = "collection_id")
    private UserCollection collection;


}