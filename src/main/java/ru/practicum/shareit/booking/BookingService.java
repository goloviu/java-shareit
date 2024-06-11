package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.List;

public interface BookingService {

    BookingDto addNewRequest(final Long userId, final BookingRequestDto bookingRequestDto);

    BookingDto ownerChangeStatus(final Long userId, final Long bookingId, final Boolean approved);

    BookingDto getBookingById(final Long userId, final Long bookingId);

    List<BookingDto> getBookingByUserId(final Long userId, final String state);

    List<BookingDto> getOwnerBookings(final Long userId, final String state);
}
