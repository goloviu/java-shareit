package ru.practicum.javashareit.exceptionhandler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ErrorResponse {
    private final String errorMessage;
}
