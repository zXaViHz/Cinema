package com.example.CRUD.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.CRUD.service.MovieService;
import com.example.CRUD.service.RatingService;
import com.example.CRUD.service.TheaterService;
import com.example.CRUD.service.UserService;
import com.example.mo.Movie;
import com.example.mo.Rating;
import com.example.mo.Theater;
import com.example.mo.Users;

@Controller
@RequestMapping("/movie")
public class MovieController {

    private final MovieService movieService;
    private final RatingService ratingService;
    private final UserService userService;
    @Autowired
    private TheaterService theaterService;

    public MovieController(MovieService movieService, UserService userService, RatingService ratingService) {
        this.movieService = movieService;
        this.ratingService = ratingService;
        this.userService = userService;
    }

    @GetMapping
    public String getAllMovies(Model model, Principal principal) {
        List<Movie> movies = movieService.getAllMovies();
        model.addAttribute("movies", movies);
        return "movie";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("movie", new Movie());
        return "movie-form";
    }

    @PostMapping("/create")
    public String createMovie(@ModelAttribute Movie movie, @RequestParam("imageFile") MultipartFile imageFile,
            RedirectAttributes redirectAttributes, Principal principal) {
        movie.setRatingCount(0);
        movie.setAverageRating(0.0);
        movie.setCinemaOwnerID(getCinemaOwnerIDFromPrincipal(principal));
        if (!imageFile.isEmpty()) {
            try {
                // Lưu tệp tải lên vào thư mục cục bộ
                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                String fileName = imageFile.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, imageFile.getBytes());
                movie.setAddress("/uploads/" + fileName); // Lưu đường dẫn ảnh vào thuộc tính address
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("message", "Không thể tải lên tệp ảnh.");
                return "redirect:/movie/new";
            }
        }
        movieService.saveMovie(movie);
        return "redirect:/movie";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, Principal principal) {
        Movie movie = movieService.getMovieById(id);
        if (movie != null) {
            Integer cinemaOwnerID = getCinemaOwnerIDFromPrincipal(principal);
            List<Theater> listTheater = theaterService.listAllByCinemaOwnerID(cinemaOwnerID);
            model.addAttribute("movie", movie);
            model.addAttribute("listTheater", listTheater);
            return "movie-form";
        }
        return "redirect:/movie";
    }

    @PostMapping("/update/{id}")
    public String updateMovie(@PathVariable Integer id, @ModelAttribute Movie movieDetails,
            @RequestParam("imageFile") MultipartFile imageFile, RedirectAttributes redirectAttributes) {
        Movie movie = movieService.getMovieById(id);
        if (movie != null) {
            if (!movie.getTitle().equalsIgnoreCase(movieDetails.getTitle())
                    && movieService.isDuplicateTitle(movieDetails.getTitle())) {
                redirectAttributes.addFlashAttribute("message", "Đã có bộ phim với title này.");
                return "redirect:/movie/edit/" + id;
            }

            if (imageFile.isEmpty() && (movie.getAddress() == null || movie.getAddress().isEmpty())) {
                redirectAttributes.addFlashAttribute("message", "Bạn hãy upload ảnh phim.");
                return "redirect:/movie/edit/" + id;
            }

            try {
                if (!imageFile.isEmpty()) {
                    String uploadDir = System.getProperty("user.dir") + "/uploads/";
                    File uploadDirFile = new File(uploadDir);
                    if (!uploadDirFile.exists()) {
                        uploadDirFile.mkdirs();
                    }

                    String fileName = imageFile.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir + fileName);
                    Files.write(filePath, imageFile.getBytes());

                    movie.setAddress("/uploads/" + fileName);
                }

                movie.updateDetails(movieDetails);
                movieService.saveMovie(movie);
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("message", "Không thể tải lên tệp ảnh.");
                return "redirect:/movie/edit/" + id;
            }
        } else {
            redirectAttributes.addFlashAttribute("message", "Bộ phim không tồn tại.");
        }
        return "redirect:/movie";
    }

    @GetMapping("/home/{id}")
    public String getMovieForHome(@PathVariable Integer id, Model model) {
        Movie movie = movieService.getMovieById(id);
        if (movie != null) {
            model.addAttribute("movie", movie);
            return "home";
        }
        return "redirect:/movie";
    }

    @GetMapping("/home")
    public String getAllMoviesForHome(Model model, Principal principal) {
        Users user = userService.getUserByUserName(principal.getName());
        model.addAttribute("user", user);

        List<Movie> movies = movieService.getAllMovies();
        List<Movie> simplifiedMovies = movies.stream()
                .map(m -> {
                    Movie simplifiedMovie = new Movie();
                    simplifiedMovie.setTitle(m.getTitle());
                    simplifiedMovie.setAddress(m.getAddress());
                    return simplifiedMovie;
                })
                .collect(Collectors.toList());
        model.addAttribute("movies", simplifiedMovies);
        return "home";
    }

    @PostMapping("/vote/{id}")
    public String voteForMovie(@PathVariable Integer id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            Users user = userService.getUsersByEmail(principal.getName());
            movieService.voteForMovie(id, user.getUserId());
            redirectAttributes.addFlashAttribute("message", "Vote successful!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/movie";
    }

    @GetMapping("/book/{movieId}")
    public String getMovieRatings(@PathVariable Integer movieId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model, Principal principal) {
        Movie movie = movieService.getMovieById(movieId);
        if (movie == null) {
            return "redirect:/error";
        }

        List<Rating> ratings = ratingService.getAllRatingsByMovieId(movieId);

        int start = Math.min(page * size, ratings.size());
        int end = Math.min((page + 1) * size, ratings.size());

        List<Rating> paginatedRatings = ratings.subList(start, end);

        model.addAttribute("movie", movie);
        model.addAttribute("ratings", paginatedRatings);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) ratings.size() / size));
        String email = principal.getName();
        Users user = userService.getUsersByEmail(email);

        model.addAttribute("user", user);
        return "book";
    }

    @GetMapping("/general/genre")
    public String searchByGenre(@RequestParam("genre") String genre, Model model, Principal p) {
        String email = p.getName();
        Users user = userService.getUsersByEmail(email);
        List<Movie> moviesByGenre = movieService.getMoviesByGenre(genre);
        List<Movie> comingSoonMovies = movieService.getAllComingSoonMovies();
        model.addAttribute("user", user);
        model.addAttribute("comingSoonMovies", comingSoonMovies);
        model.addAttribute("movies", moviesByGenre);
        return "home";
    }

    @GetMapping("/general/languages")
    public String searchByLanguages(@RequestParam("languages") String languages, Model model, Principal principal) {
        String email = principal.getName();
        Users user = userService.getUsersByEmail(email);

        // Xử lý chuỗi languages để lấy danh sách các từ khóa
        String languageKeyword = languages.split(" - ")[0].trim(); // Lấy phần trước dấu "-"
        languageKeyword = languageKeyword.toLowerCase(); // Chuyển về chữ thường để so sánh không phân biệt hoa/thường

        // Lấy danh sách phim theo từ khóa ngôn ngữ
        List<Movie> moviesByLanguages = movieService.getMoviesByLanguagesKeyword(languageKeyword);
        List<Movie> comingSoonMovies = movieService.getAllComingSoonMovies();

        // Thêm dữ liệu vào model
        model.addAttribute("user", user);
        model.addAttribute("comingSoonMovies", comingSoonMovies);
        model.addAttribute("movies", moviesByLanguages);
        model.addAttribute("languages", languages); // Gửi từ khóa ngôn ngữ về view

        return "home";
    }

    @GetMapping("/general/year")
    public String searchByYear(@RequestParam("year") String year, Model model, Principal principal) {
        String email = principal.getName();
        Users user = userService.getUsersByEmail(email);

        List<Movie> moviesByYear;
        if (year.equals("before2015")) {
            // Lấy phim trước năm 2015
            moviesByYear = movieService.getMoviesBeforeReleaseYear(2015);
        } else {
            // Lấy phim trong khoảng năm
            int selectedYear = Integer.parseInt(year);
            moviesByYear = movieService.getMoviesByReleaseYearRange(selectedYear, selectedYear);
        }

        List<Movie> comingSoonMovies = movieService.getAllComingSoonMovies();

        // Thêm dữ liệu vào model
        model.addAttribute("user", user);
        model.addAttribute("comingSoonMovies", comingSoonMovies);
        model.addAttribute("movies", moviesByYear);
        model.addAttribute("year", year); // Gửi năm đã chọn về view

        return "home";
    }

    private Integer getCinemaOwnerIDFromPrincipal(Principal principal) {
        Users user = userService.getUsersByEmail(principal.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user.getUserId(); // Ensure this returns the correct ID for cinema owner
    }

    @GetMapping("/general/search")
    public String searchMovies(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "languages", required = false) String languages,
            @RequestParam(value = "year", required = false) String year,
            Model model,
            Principal principal) {

        String email = principal.getName();
        Users user = userService.getUsersByEmail(email);

        // Lấy danh sách phim dựa trên các tiêu chí tìm kiếm
        List<Movie> movies = movieService.searchMovies(keyword, genre, languages, year);
        List<Movie> comingSoonMovies = movieService.getAllComingSoonMovies();

        // Thêm dữ liệu vào model
        model.addAttribute("user", user);
        model.addAttribute("comingSoonMovies", comingSoonMovies);
        model.addAttribute("movies", movies);
        model.addAttribute("keyword", keyword);
        model.addAttribute("genre", genre);
        model.addAttribute("languages", languages);
        model.addAttribute("year", year);

        return "home";
    }
}