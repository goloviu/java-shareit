package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exceptionhandler.exceptions.DateTimeBookingException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingControllerGateWay {
    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @Positive @RequestParam(name = "from", defaultValue = "1") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("GateWay GET запрос на получение своих бронирований {}, userId={}, from={}, size={}",
                stateParam, userId, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestBody @Valid BookItemRequestDto requestDto) {
        log.info("GateWay POST запрос на создание бронирования {}, userId={}", requestDto, userId);
        checkBooking(requestDto);
        return bookingClient.bookItem(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @PathVariable Long bookingId) {
        log.info("GateWay GET запрос на получение бронирования по ID {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> bookingOwnerChangeStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                           @PathVariable Long bookingId,
                                                           @RequestParam(value = "approved") Boolean approved) {
        log.info("GateWay PATCH запрос от владельца по ID {} на изменение статуса запроса бронирования ID {} на \'{}\'",
                userId, bookingId, approved);
        return bookingClient.changeBookingStatus(userId, bookingId, approved);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                   @Positive @RequestParam(name = "from", defaultValue = "1") Integer from,
                                                   @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS"));
        log.info("GateWay GET запрос от владельца ID {} на получение своих забронированных вещей. Правило получения: {}",
                userId, state);
        return bookingClient.getOwnerBookings(userId, state, from, size);
    }

    private void checkBooking(final BookItemRequestDto bookingRequestDto) {
        LocalDateTime start = bookingRequestDto.getStart();
        LocalDateTime end = bookingRequestDto.getEnd();

        if (start == null || end == null) {
            throw new DateTimeBookingException("Значения начала бронирования и конца бронирования должны быть указаны");
        }

        if (start.isBefore(LocalDateTime.now()) || end.isBefore(LocalDateTime.now())) {
            throw new DateTimeBookingException("Дата и время бронирования не может быть в прошлом: " + start);
        }

        if (start.isAfter(end)) {
            throw new DateTimeBookingException("Дата и время начала бронирования не может быть после окончания: "
                    + start);
        }

        if (start.equals(end)) {
            throw new DateTimeBookingException("Дата и время начала и конца бронирования не должны быть равны");
        }
    }
}
