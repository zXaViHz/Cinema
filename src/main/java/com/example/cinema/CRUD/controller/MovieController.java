package com.example.cinema.CRUD.controller;

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

import com.example.cinema.CRUD.service.MovieService;

import com.example.cinema.mo.Movie;

@Controller
@RequestMapping("/movie")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;

    }

    @GetMapping
    public String getAllMovies(Model model, Principal principal) {
        List<Movie> movies = movieService.getAllMovies();
        model.addAttribute("movies", movies);
        return "movie";
    }

    @PostMapping("/create")
    public String createMovie(@ModelAttribute Movie movie, @RequestParam("imageFile") MultipartFile imageFile,
            RedirectAttributes redirectAttributes, Principal principal) {
        movie.setRatingCount(0);
        movie.setAverageRating(0.0);
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
            model.addAttribute("movie", movie);
            return "movie-form";
        }
        return "redirect:/movie";
    }

    @PostMapping("/update/{id}")
    public String updateMovie(@PathVariable Integer id, @ModelAttribute Movie movieDetails,
            @RequestParam("imageFile") MultipartFile imageFile, RedirectAttributes redirectAttributes) {
        Movie movie = movieService.getMovieById(id);
        if (movie != null) {
            if (!imageFile.isEmpty()) {
                try {
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
                    return "redirect:/movie/edit/" + id;
                }
            }
            movie.updateDetails(movieDetails);
            movieService.saveMovie(movie);
        }
        return "redirect:/movie";
    }

    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable Integer id) {
        movieService.deleteMovie(id);
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

}
