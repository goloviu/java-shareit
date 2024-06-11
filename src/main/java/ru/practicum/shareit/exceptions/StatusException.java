package ru.practicum.shareit.exceptions;

public class StatusException extends RuntimeException {
    public StatusException(String message) {
        super(message);
    }

    public StatusException(String message, Throwable cause) {
        super(message, cause);
    }
}