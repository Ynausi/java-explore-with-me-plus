package ru.practicum.exception;

public class CategoryDeleteConflictException extends RuntimeException {
    public CategoryDeleteConflictException(String message) {
        super(message);
    }
}
