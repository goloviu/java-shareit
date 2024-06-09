package ru.practicum.shareit.exceptions;

public class DateTimeBookingException extends RuntimeException {
    public DateTimeBookingException(String message) {
        super(message);
    }

    public DateTimeBookingException(String message, Throwable cause) {
        super(message, cause);
    }
}