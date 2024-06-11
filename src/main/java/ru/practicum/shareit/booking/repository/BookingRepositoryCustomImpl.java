package ru.practicum.shareit.booking.repository;

import ru.practicum.shareit.booking.Booking;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

public class BookingRepositoryCustomImpl implements BookingRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Booking> findLastBooking(final Long itemId, final LocalDateTime currentTime) {
        return entityManager.createQuery("SELECT b FROM Booking b " +
                        "JOIN FETCH b.item " +
                        "WHERE b.item.id = ?1 " +
                        "AND b.status = 'APPROVED' " +
                        "AND b.start < ?2" +
                        "ORDER BY b.end DESC").setParameter(1, itemId).setParameter(2, currentTime).setMaxResults(1)
                .getResultList();
    }

    @Override
    public List<Booking> findNextBooking(final Long itemId, final LocalDateTime currentTime) {
        return entityManager.createQuery("SELECT b FROM Booking b " +
                        "JOIN FETCH b.item " +
                        "WHERE b.item.id = ?1 " +
                        "AND b.status = 'APPROVED' " +
                        "AND b.start > ?2" +
                        "ORDER BY b.end").setParameter(1, itemId).setParameter(2, currentTime).setMaxResults(1)
                .getResultList();
    }
}
