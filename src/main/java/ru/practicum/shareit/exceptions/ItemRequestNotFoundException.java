package ru.practicum.shareit.exceptions;

public class ItemRequestNotFoundException extends RuntimeException {
    public ItemRequestNotFoundException(String message) {
        super(message);
    }

    public ItemRequestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}