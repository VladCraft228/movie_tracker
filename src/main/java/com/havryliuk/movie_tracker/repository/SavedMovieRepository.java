package com.havryliuk.movie_tracker.repository;

import com.havryliuk.movie_tracker.entity.SavedMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedMovieRepository extends JpaRepository<SavedMovie, Long> {
    boolean existsByTmdbIdAndCollectionId(Long tmdbId, Long collectionId);
    List<SavedMovie> findByTmdbId(Long tmdbId);
}