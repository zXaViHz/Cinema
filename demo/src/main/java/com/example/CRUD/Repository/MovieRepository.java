package com.example.CRUD.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.mo.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    boolean existsByTitle(String title);

    List<Movie> findByStatusMovieAndAddressNotNull(String statusMovie);

    List<Movie> findByGenre(String genre);

    List<Movie> findByCinemaOwnerID(Integer cinemaOwnerID);

    List<Movie> findByLanguagesContainingIgnoreCase(String keyword);

    @Query("SELECT m FROM Movie m WHERE YEAR(m.releaseDate) BETWEEN :startYear AND :endYear")
    List<Movie> findByReleaseYearBetween(@Param("startYear") int startYear, @Param("endYear") int endYear);

    @Query("SELECT m FROM Movie m WHERE YEAR(m.releaseDate) < :year")
    List<Movie> findByReleaseYearBefore(@Param("year") int year);

    List<Movie> findByTitleContainingIgnoreCase(String titleKeyword);}