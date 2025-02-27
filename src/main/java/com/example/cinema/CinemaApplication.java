package com.example.cinema;

import com.example.cinema.CRUD.Repository.MovieRepository;
import com.example.cinema.mo.Movie;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class CinemaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinemaApplication.class, args);
	}

	@Bean
	CommandLineRunner run(MovieRepository movieRepository) {
		return args -> {
			List<Movie> movies = movieRepository.findAll();
			if (movies.isEmpty()) {
				System.out.println("Database không có dữ liệu phim.");
			} else {
				System.out.println("Danh sách phim trong database:");
				movies.forEach(System.out::println);
			}
		};
	}
}
