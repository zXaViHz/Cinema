package com.example.cinema.CRUD.controller;

public class TheaterNotFoundException extends Throwable {
    public TheaterNotFoundException(String message) {
        super(message);
    }
}