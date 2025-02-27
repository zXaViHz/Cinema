package com.example.cinema.CRUD.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.cinema.CRUD.Repository.SeatRepository;
import com.example.cinema.mo.Seat;

@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;

    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    public List<Object[]> getBookedSeats(Integer screeningRoomId, Integer showtimeId) {
        return seatRepository.findBookedSeats(screeningRoomId, showtimeId);
    }
}
