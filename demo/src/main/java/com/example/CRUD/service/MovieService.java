package com.example.CRUD.service;

import java.util.List;

import com.example.mo.Movie;

public interface MovieService {
    List<Movie> getAllMovies();

    void voteForMovie(Integer movieId, Integer userId);

    Movie getMovieById(Integer id);

    Movie saveMovie(Movie movie);

    void deleteMovie(Integer id);

    void updateAverageRating(Integer movieID);

    boolean isDuplicateTitle(String translatedTitle);

    boolean movieExistsByTitle(String title);

    List<Movie> getAllComingSoonMovies();

    List<Movie> getMoviesByGenre(String genre);

    List<Movie> getAllMoviesForHomeCinemaOwner(int cinemaOwnerId);

    // Tìm kiếm phim theo từ khóa trong languages
    List<Movie> getMoviesByLanguagesKeyword(String keyword);

    List<Movie> findByCinemaOwnerID(int theaterId);

    List<Movie> getMoviesByReleaseYearRange(int startYear, int endYear);

    List<Movie> getMoviesBeforeReleaseYear(int year);

    List<Movie> searchMovies(String keyword, String genre, String languages, String year);

}