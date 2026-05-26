package ru.practicum.exception;

public class CategoryExistByName extends RuntimeException {
    public CategoryExistByName(String message) {
        super(message);
    }
}
