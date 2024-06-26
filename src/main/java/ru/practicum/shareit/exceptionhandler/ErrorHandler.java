package ru.practicum.shareit.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exceptions.*;

@RestControllerAdvice("ru.practicum.shareit")
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

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse itemNotFoundHandle(ItemNotFoundException exception) {
        log.warn("Предмет не найден в базе данных. \n {}", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse itemNotAvailableForBookingHandle(ItemNotAvailableForBookingException exception) {
        log.warn("Предмет не доступен для бронирования. \n {}", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse dateTimeWrongParameterHandle(DateTimeBookingException exception) {
        log.warn("Параметры для запроса на бронирование введены неверно. \n {}", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse bookingNotFoundHandle(BookingNotFoundException exception) {
        log.warn("Бронирование не найдено. \n {}", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse incorrectStatusHandle(StatusException exception) {
        log.warn("Произошла ошибка с статусом бронирования. \n {}", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse wrongAddCommentToBookingHandle(CommentException exception) {
        log.warn("Произошла ошибка с комментарием. \n {}", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse itemRequestNotFoundHandle(ItemRequestNotFoundException exception) {
        log.warn("Запрос на предмет не найден в базе данных. \n {}", exception);
        return new ErrorResponse(exception.getMessage());
    }
}