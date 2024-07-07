package ru.practicum.shareit.exceptionhandler.exceptions;

public class DateTimeBookingException extends RuntimeException {
    public DateTimeBookingException(String message) {
        super(message);
    }
}