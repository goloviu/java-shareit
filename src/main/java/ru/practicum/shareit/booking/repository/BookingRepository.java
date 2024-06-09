package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatusType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, BookingRepositoryCustom {

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.booker " +
            "WHERE b.booker.id = ?1 " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByBookerId(final Long userId);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.booker " +
            "WHERE b.booker.id = ?1 " +
            "AND b.end > ?2 " +
            "AND b.start < ?2")
    List<Booking> findAllCurrentBookings(final Long userId, final LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.booker " +
            "WHERE (b.booker.id = ?1 " +
            "OR b.item.owner = ?1) " +
            "AND b.end < ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> findAllPastBookings(final Long userId, final LocalDateTime dateTimeNow);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.booker " +
            "WHERE (b.booker.id = ?1 " +
            "OR b.item.owner = ?1) " +
            "ORDER BY b.start DESC")
    List<Booking> findAllFutureBookings(final Long userId);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.booker " +
            "WHERE b.booker.id = ?1 " +
            "AND b.status = ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByBookerAndStatus(final Long userId, final BookingStatusType state);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner = ?1 " +
            "ORDER BY b.start DESC")
    List<Booking> findAllOwnerItemBookings(final Long userId);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner = ?1 " +
            "AND b.end > ?2 " +
            "AND b.start < ?2")
    List<Booking> findAllOwnerCurrentBookings(final Long userId, final LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner = ?1 " +
            "AND b.end < ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> findAllPastOwnerItemBookings(final Long userId, final LocalDateTime dateTime);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner = ?1 " +
            "AND b.start > ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> findAllFutureOwnerItemBookings(final Long userId, final LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner = ?1 " +
            "AND b.status = ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> findAllOwnerItemBookedByStatus(final Long userId, final BookingStatusType state);

    Optional<Booking> findFirstByItemId(final Long itemId);
}
