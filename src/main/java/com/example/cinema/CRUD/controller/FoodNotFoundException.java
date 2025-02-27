package com.example.cinema.CRUD.controller;

public class FoodNotFoundException extends Throwable {
    public FoodNotFoundException(String message) {
       super(message);
    }
}