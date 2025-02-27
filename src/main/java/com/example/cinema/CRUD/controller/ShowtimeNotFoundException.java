package com.example.cinema.CRUD.controller;

public class ShowtimeNotFoundException extends Throwable {
    public ShowtimeNotFoundException(String message) {
        super(message);
    }
}