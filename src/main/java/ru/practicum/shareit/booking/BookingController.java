package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto bookingAddNewRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestBody @Valid BookingRequestDto bookingRequestDto) {
        log.info("Получен POST запрос на создание запроса бронирования от пользователя ID {}, запрос: \n {}",
                userId, bookingRequestDto);
        return bookingService.addNewRequest(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto ownerChangeStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @PathVariable Long bookingId,
                                        @RequestParam(value = "approved") Boolean approved) {
        log.info("Получен PATCH запрос от владельца по ID {} на изменение статуса запроса бронирования ID {} на \'{}\'",
                userId, bookingId, approved);
        return bookingService.ownerChangeStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable Long bookingId) {
        log.info("Получен GET запрос от пользователя по ID {} на получение бронирования ID {}", userId, bookingId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookingByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получен GET запрос от пользователя по ID {} на получение своих бронирований. Правило получения: {}",
                userId, state);
        return bookingService.getBookingByUserId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получен GET запрос от владельца ID {} на получение своих забронированных вещей. Правило получения: {}",
                userId, state);
        return bookingService.getOwnerBookings(userId, state);
    }
}
