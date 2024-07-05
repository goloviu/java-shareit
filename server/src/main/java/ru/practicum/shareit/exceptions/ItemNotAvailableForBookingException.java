package ru.practicum.shareit.exceptions;

public class ItemNotAvailableForBookingException extends RuntimeException {
    public ItemNotAvailableForBookingException(String message) {
        super(message);
    }
}