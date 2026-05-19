package com.havryliuk.movie_tracker.service;

import com.havryliuk.movie_tracker.dto.TmdbMovieDTO;
import com.havryliuk.movie_tracker.dto.TmdbResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TmdbService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://api.themoviedb.org/3";

    @Value("${tmdb.api.key}")
    private String apiKey;

    // 1. Пошук фільмів
    public List<TmdbMovieDTO> searchMovies(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String urlUk = String.format("%s/search/movie?api_key=%s&query=%s&language=uk-UA", BASE_URL, apiKey, encodedQuery);

            TmdbResponse responseUk = restTemplate.getForObject(urlUk, TmdbResponse.class);

            if (responseUk == null || responseUk.getResults() == null) {
                return List.of();
            }

            List<TmdbMovieDTO> ukMovies = responseUk.getResults();

            enrichWithEnglishOverviewsIfNeeded(ukMovies, encodedQuery);

            return ukMovies;

        } catch (Exception e) {
            System.err.println("Помилка пошуку TMDB API: " + e.getMessage());
            return List.of();
        }
    }

    // 2. Деталі фільму
    public TmdbMovieDTO getMovieDetails(Long tmdbId) {
        String urlUk = String.format("%s/movie/%d?api_key=%s&language=uk-UA&append_to_response=videos&include_video_language=uk,en", BASE_URL, tmdbId, apiKey);

        try {
            TmdbMovieDTO movie = restTemplate.getForObject(urlUk, TmdbMovieDTO.class);

            if (movie != null && isOverviewMissing(movie)) {
                String urlEn = String.format("%s/movie/%d?api_key=%s&language=en-US", BASE_URL, tmdbId, apiKey);
                TmdbMovieDTO enMovie = restTemplate.getForObject(urlEn, TmdbMovieDTO.class);

                if (enMovie != null && !isOverviewMissing(enMovie)) {
                    movie.setOverview(enMovie.getOverview());
                }
            }
            return movie;
        } catch (Exception e) {
            System.err.println("Помилка отримання деталей: " + e.getMessage());
            return null;
        }
    }

    private void enrichWithEnglishOverviewsIfNeeded(List<TmdbMovieDTO> ukMovies, String encodedQuery) {
        // Перевіряємо, чи потрібна нам взагалі англійська база
        boolean needsEnglish = ukMovies.stream().anyMatch(this::isOverviewMissing);
        if (!needsEnglish) return;

        String urlEn = String.format("%s/search/movie?api_key=%s&query=%s&language=en-US", BASE_URL, apiKey, encodedQuery);
        TmdbResponse responseEn = restTemplate.getForObject(urlEn, TmdbResponse.class);

        if (responseEn == null || responseEn.getResults() == null) return;

        // Зшиваємо результати
        for (TmdbMovieDTO ukMovie : ukMovies) {
            if (isOverviewMissing(ukMovie)) {
                responseEn.getResults().stream()
                        .filter(enMovie -> enMovie.getId().equals(ukMovie.getId()))
                        .findFirst()
                        .ifPresent(enMovie -> ukMovie.setOverview(enMovie.getOverview()));
            }
        }
    }

    // Прибираємо дублювання коду (DRY)
    private boolean isOverviewMissing(TmdbMovieDTO movie) {
        return movie.getOverview() == null || movie.getOverview().trim().isEmpty();
    }
}