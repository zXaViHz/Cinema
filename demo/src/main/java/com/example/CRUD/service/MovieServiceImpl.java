package com.example.CRUD.service;

import java.text.Normalizer;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.CRUD.Repository.MovieRepository;
import com.example.CRUD.Repository.RatingRepository;
import com.example.CRUD.Repository.UserRepository;
import com.example.mo.Movie;
import com.example.mo.Rating;
import com.example.mo.Users;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    private String normalizeString(String str) {
        if (str == null) {
            return "";
        }
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase().trim();
    }

    @Autowired
    public MovieServiceImpl(MovieRepository movieRepository, UserRepository userRepository,
            RatingRepository ratingRepository) {
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Override
    public Movie getMovieById(Integer movieID) {
        return movieRepository.findById(movieID).orElse(null);
    }

    @Override
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Override
    public void deleteMovie(Integer id) {
        movieRepository.deleteById(id);
    }

    public void voteForMovie(Integer movieId, Integer userId) {
        Movie movie = getMovieById(movieId);
        Users user = userRepository.findById(userId).orElse(null);

        if (movie == null || user == null) {
            throw new RuntimeException("Movie or User not found");
        }

        if (movie.getCinemaOwnerVotes().contains(userId)) {
            throw new RuntimeException("User has already voted for this movie");
        }

        movie.addVote(userId);
        saveMovie(movie);

        // Check if all cinema owners have voted
        long totalCinemaOwners = userRepository.countByRole("CINEMA_OWNER");
        long totalVotesForMovie = movie.getCinemaOwnerVotes().size();

        if (totalVotesForMovie >= totalCinemaOwners) {
            movie.setStatusMovie("END");
            movieRepository.save(movie);
        }
    }

    @Override
    public void updateAverageRating(Integer movieID) {
        List<Rating> ratings = ratingRepository.findByMovie_MovieID(movieID);
        double averageRating = ratings.stream().mapToInt(Rating::getScore).average().orElse(0.0);
        Movie movie = getMovieById(movieID);
        if (movie != null) {
            movie.setAverageRating(averageRating);
            movie.setRatingCount(ratings.size());
            movieRepository.save(movie);
        }
    }

    public boolean isDuplicateTitle(String translatedTitle) {
        List<Movie> movies = movieRepository.findAll();
        for (Movie movie : movies) {
            if (translatedTitle.equalsIgnoreCase(movie.getTitle())) {
                return true; // Trả về true nếu tìm thấy sự trùng lặp
            }
        }
        return false; // Trả về false nếu không tìm thấy sự trùng lặp
    }

    public boolean movieExistsByTitle(String title) {
        return movieRepository.existsByTitle(title);
    }

    @Override
    public List<Movie> getAllComingSoonMovies() {
        return movieRepository.findByStatusMovieAndAddressNotNull(Movie.StatusMovie.COMING_SOON.getStatus());
    }

    public List<Movie> getMoviesByGenre(String genre) {
        return movieRepository.findByGenre(genre);
    }

    @Override
    public List<Movie> getAllMoviesForHomeCinemaOwner(int cinemaOwnerId) {
        return movieRepository.findByCinemaOwnerID(cinemaOwnerId);
    }

    @Override
    public List<Movie> getMoviesByLanguagesKeyword(String keyword) {
        // Lấy tất cả các phim từ cơ sở dữ liệu
        List<Movie> allMovies = movieRepository.findAll();

        // Lọc các phim có phần trước dấu "-" trong languages khớp với từ khóa
        return allMovies.stream()
                .filter(movie -> {
                    if (movie.getLanguages() != null) {
                        // Lấy phần trước dấu "-" trong chuỗi languages
                        String[] parts = movie.getLanguages().split(" - ");
                        String languageBeforeDash = parts[0].trim().toLowerCase();
                        return languageBeforeDash.contains(keyword.toLowerCase());
                    }
                    return false;
                })
                .toList();
    }

    @Override
    public List<Movie> getMoviesByReleaseYearRange(int startYear, int endYear) {
        return movieRepository.findByReleaseYearBetween(startYear, endYear);
    }

    @Override
    public List<Movie> getMoviesBeforeReleaseYear(int year) {
        return movieRepository.findByReleaseYearBefore(year);
    }

    @Override
    public List<Movie> searchMovies(String keyword, String genre, String languages, String year) {
        List<Movie> allMovies = movieRepository.findAll();

        return allMovies.stream()
                .filter(movie -> {
                    boolean matches = true;

                    // Lọc theo keyword (nếu có)
                    if (keyword != null && !keyword.isEmpty()) {
                        if (movie.getTitle() != null) {
                            String normalizedTitle = normalizeString(movie.getTitle());
                            String normalizedKeyword = normalizeString(keyword);
                            matches = matches && normalizedTitle.contains(normalizedKeyword);
                        } else {
                            matches = false;
                        }
                    }

                    // Lọc theo genre (nếu có)
                    if (genre != null && !genre.isEmpty()) {
                        if (movie.getGenre() != null) {
                            matches = matches && genre.equalsIgnoreCase(movie.getGenre());
                        } else {
                            matches = false;
                        }
                    }

                    // Lọc theo languages (nếu có)
                    if (languages != null && !languages.isEmpty()) {
                        if (movie.getLanguages() != null) {
                            String[] parts = movie.getLanguages().split(" - ");
                            String languageBeforeDash = parts[0].trim().toLowerCase();
                            matches = matches && languageBeforeDash.contains(languages.toLowerCase());
                        } else {
                            matches = false;
                        }
                    }

                    // Lọc theo year (nếu có)
                    if (year != null && !year.isEmpty()) {
                        if (year.equals("before2015")) {
                            matches = matches && movie.getReleaseDate().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                    .getYear() < 2015;
                        } else {
                            int selectedYear = Integer.parseInt(year);
                            matches = matches && movie.getReleaseDate().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                    .getYear() == selectedYear;
                        }
                    }

                    return matches;
                })
                .toList();
    }

    public List<Movie> findByCinemaOwnerID(int theaterId) {
        return movieRepository.findByCinemaOwnerID(theaterId);
    }
}