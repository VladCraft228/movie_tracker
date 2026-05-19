package com.havryliuk.movie_tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class UserCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;


    // Зв'язок One-to-Many (Одна колекція містить багато фільмів)
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavedMovie> movies = new ArrayList<>();


    // Метод-помічник для безпечного додавання фільму в колекцію
    public void addMovie(SavedMovie movie) {
        movies.add(movie);
        movie.setCollection(this);
    }

    // Метод-помічник для безпечного видалення фільму
    public void removeMovie(SavedMovie movie) {
        movies.remove(movie);
        movie.setCollection(null);
    }
}