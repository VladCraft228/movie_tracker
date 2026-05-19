package com.havryliuk.movie_tracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieDTO {
    private Long id;
    private String title;
    private String overview;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("vote_average")
    private Double rating;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("videos")
    private VideosDTO videos;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideosDTO {
        private List<VideoDTO> results;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoDTO {
        private String type;
        private String site;
        private String key;
    }

    public Double getRating() {
        if (this.rating == null) return null;
        return Math.round(this.rating * 10.0) / 10.0;
    }

    public String getTrailerKey() {
        if (this.videos != null && this.videos.getResults() != null) {
            for (VideoDTO video : this.videos.getResults()) {
                if ("YouTube".equals(video.getSite()) && "Trailer".equals(video.getType())) {
                    return video.getKey();
                }
            }
        }
        return null;
    }
}