package ru.practicum.shareit.booking.repository;

import ru.practicum.shareit.booking.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepositoryCustom {

    List<Booking> findLastBooking(final Long itemId, final LocalDateTime currentTime);

    List<Booking> findNextBooking(final Long itemId, final LocalDateTime currentTime);
}
