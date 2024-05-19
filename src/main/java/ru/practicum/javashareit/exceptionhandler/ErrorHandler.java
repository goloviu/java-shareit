package ru.practicum.javashareit.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.javashareit.exceptions.PermissionException;
import ru.practicum.javashareit.exceptions.UserAlreadyExistsException;
import ru.practicum.javashareit.exceptions.UserNotFoundException;

@RestControllerAdvice("ru.practicum.javashareit")
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse unexpectedErrorHandle(Throwable exception) {
        log.warn("Произошла непредвиденная ошибка \n {}", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse existUserExceptionHandle(UserAlreadyExistsException exception) {
        log.warn("Пользователь уже существует \n {}", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationExceptionHandle(MethodArgumentNotValidException exception) {
        log.warn("Данные не прошли валидацию \n {}", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse userNotFoundHandle(UserNotFoundException exception) {
        log.warn("Пользователь не найден \n {}", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse permissionHandle(PermissionException exception) {
        log.warn("Недостаточно прав для исполнения операции \n {}", exception);
        return new ErrorResponse(exception.getMessage());
    }
}
