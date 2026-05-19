package com.havryliuk.movie_tracker.controller;

import com.havryliuk.movie_tracker.dto.TmdbMovieDTO;
import com.havryliuk.movie_tracker.entity.SavedMovie;
import com.havryliuk.movie_tracker.entity.UserCollection;
import com.havryliuk.movie_tracker.repository.SavedMovieRepository;
import com.havryliuk.movie_tracker.repository.UserCollectionRepository;
import com.havryliuk.movie_tracker.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MovieController {

    private final TmdbService tmdbService;
    private final UserCollectionRepository collectionRepository;
    private final SavedMovieRepository movieRepository;

    // Головна сторінка з ДИНАМІЧНИМ СОРТУВАННЯМ
    @GetMapping("/")
    public String index(Model model, String query,
                        @RequestParam(defaultValue = "rating") String sortBy, String filter) {

        if (query != null && !query.isEmpty()) {
            model.addAttribute("searchResults", tmdbService.searchMovies(query));
        }

        List<UserCollection> collections = collectionRepository.findAll();
        filterAndSortCollections(collections, filter, sortBy);

        model.addAttribute("collections", collections);
        model.addAttribute("currentSort", sortBy);
        model.addAttribute("currentFilter", filter);
        return "index";
    }

    @PostMapping("/collections/add")
    public String createCollection(@RequestParam String name) {
        UserCollection collection = new UserCollection();
        collection.setName(name);
        collectionRepository.save(collection);
        return "redirect:/";
    }

    @PostMapping("/movies/save")
    public String saveMovie(@RequestParam Long collectionId, @RequestParam Long tmdbId) {
        if (movieRepository.existsByTmdbIdAndCollectionId(tmdbId, collectionId)) {
            return "redirect:/?error=duplicate";
        }

        UserCollection collection = collectionRepository.findById(collectionId).orElseThrow();
        TmdbMovieDTO apiMovie = tmdbService.getMovieDetails(tmdbId);
        if (apiMovie == null) return "redirect:/?error=notfound";

        SavedMovie movie = new SavedMovie();
        movie.setTmdbId(tmdbId);
        collection.addMovie(movie);
        movie.fillFromApi(apiMovie);
        collectionRepository.save(collection);
        return "redirect:/";
    }

    // Сторінка деталей: передаємо інформацію про оцінки
    @GetMapping("/movies/details/{tmdbId}")
    public String movieDetails(@PathVariable Long tmdbId, Model model) {
        TmdbMovieDTO movie = tmdbService.getMovieDetails(tmdbId);
        if (movie == null) return "redirect:/?error=notfound";

        // Шукаємо, чи збережений цей фільм хоча б десь у базі
        List<SavedMovie> savedInstances = movieRepository.findByTmdbId(tmdbId);
        Double existingUserRating = null;
        if (!savedInstances.isEmpty()) {
            existingUserRating = savedInstances.get(0).getUserRating();
        }

        model.addAttribute("movie", movie);
        model.addAttribute("trailerKey", movie.getTrailerKey());
        model.addAttribute("collections", collectionRepository.findAll());
        model.addAttribute("userRating", existingUserRating);
        model.addAttribute("isSaved", !savedInstances.isEmpty());

        return "movie-details";
    }

    // Оновлення оцінки з головної сторінки
    @PostMapping("/movies/update-rating/{id}")
    public String updateMovieRating(@PathVariable Long id, @RequestParam Double userRating) {
        SavedMovie movie = movieRepository.findById(id).orElseThrow();
        movie.setUserRating(userRating);
        movieRepository.save(movie);
        return "redirect:/";
    }

    // Оновлення оцінки ПРЯМО ЗІ СТОРІНКИ ДЕТАЛЕЙ
    @PostMapping("/movies/update-rating-details/{tmdbId}")
    public String updateMovieRatingDetails(@PathVariable Long tmdbId, @RequestParam Double userRating) {
        List<SavedMovie> savedInstances = movieRepository.findByTmdbId(tmdbId);
        for (SavedMovie movie : savedInstances) {
            movie.setUserRating(userRating);
            movieRepository.save(movie);
        }
        return "redirect:/movies/details/" + tmdbId + "?ratingUpdated=true";
    }

    @PostMapping("/collections/update/{id}")
    public String updateCollectionName(@PathVariable Long id, @RequestParam String newName) {
        UserCollection collection = collectionRepository.findById(id).orElseThrow();
        collection.setName(newName);
        collectionRepository.save(collection);
        return "redirect:/";
    }

    @PostMapping("/collections/delete/{id}")
    public String deleteCollection(@PathVariable Long id) {
        collectionRepository.deleteById(id);
        return "redirect:/";
    }

    // Як має бути (з використанням removeMovie)
    @PostMapping("/movies/delete/{id}")
    public String deleteMovie(@PathVariable Long id) {
        SavedMovie movie = movieRepository.findById(id).orElseThrow();
        UserCollection collection = movie.getCollection();

        if (collection != null) {
            collection.removeMovie(movie);
            collectionRepository.save(collection);
        }

        return "redirect:/";
    }

    private void filterAndSortCollections(List<UserCollection> collections, String filter, String sortBy) {
        for (UserCollection col : collections) {
            if (filter != null && !filter.isEmpty()) {
                List<SavedMovie> filtered = col.getMovies().stream()
                        .filter(m -> m.getTitle().toLowerCase().contains(filter.toLowerCase().trim()))
                        .toList();
                col.setMovies(new java.util.ArrayList<>(filtered));
            }

            if ("title".equals(sortBy)) {
                col.getMovies().sort((m1, m2) -> m1.getTitle().compareToIgnoreCase(m2.getTitle()));
            } else {
                col.getMovies().sort((m1, m2) -> {
                    Double r1 = m1.getTmdbRating() != null ? m1.getTmdbRating() : 0.0;
                    Double r2 = m2.getTmdbRating() != null ? m2.getTmdbRating() : 0.0;
                    return r2.compareTo(r1);
                });
            }
        }
    }
}