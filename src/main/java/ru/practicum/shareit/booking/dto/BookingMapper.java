package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatusType;

public class BookingMapper {

    public static Booking bookingRequestDtoToBooking(final BookingRequestDto bookingRequestDto) {
        return Booking.builder()
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .status(BookingStatusType.WAITING)
                .build();
    }

    public static BookingDto bookingToBookingDto(final Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart().toString())
                .end(booking.getEnd().toString())
                .status(booking.getStatus())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .build();
    }
}
