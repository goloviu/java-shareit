package ru.practicum.shareit.exceptions;

public class ItemNotAvailableForBookingException extends RuntimeException {
    public ItemNotAvailableForBookingException(String message) {
        super(message);
    }

    public ItemNotAvailableForBookingException(String message, Throwable cause) {
        super(message, cause);
    }
}