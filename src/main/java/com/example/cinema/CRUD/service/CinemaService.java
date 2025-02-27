package com.example.cinema.CRUD.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.cinema.CRUD.Repository.CinemaOwnerRepository;
import com.example.cinema.mo.CinemaOwner;

@Service
public class CinemaService {
    @Autowired
    private CinemaOwnerRepository cinemaOwnerRepository;

    public List<CinemaOwner> getCinemaOwnerByAdmins(){
        return cinemaOwnerRepository.findAll();
    }

    public List<CinemaOwner> searchCinemasOwnerByCinemaName(String cinemaName) {
        if (cinemaName == null || cinemaName.isEmpty()) {
            return cinemaOwnerRepository.findAll();
        } else {
            return cinemaOwnerRepository.findByCinemaNameContainingIgnoreCase(cinemaName);
        }
    }

    public CinemaOwner getCinemaOwnerById(int cinemaOwnerId) {
        return cinemaOwnerRepository.findById(cinemaOwnerId).orElse(null);
    }

    public void saveCinemaOwner(CinemaOwner cinemaOwner) {
        cinemaOwnerRepository.save(cinemaOwner);
    }
}
