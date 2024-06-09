package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingStorage;
    private final ItemRepository itemStorage;
    private final UserRepository userStorage;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingStorage, ItemRepository itemStorage, UserRepository userStorage) {
        this.bookingStorage = bookingStorage;
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public BookingDto addNewRequest(Long userId, BookingRequestDto bookingRequestDto) {
        Booking bookingRequest = BookingMapper.bookingRequestDtoToBooking(bookingRequestDto);
        checkBooking(bookingRequestDto);
        Long itemId = bookingRequestDto.getItemId();

        Item itemFromDb = itemStorage.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Не найден предмет по ID " + itemId));

        if (itemFromDb.getOwner().equals(userId)) {
            throw new PermissionException("Пользователь не может запросить бронирование у себя. Пользователь ID "
                    + userId + " владелец предмета ID " + itemFromDb.getOwner());
        }

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден по ID " + userId));

        checkAvailableItem(itemFromDb);
        bookingRequest.setItem(itemFromDb);
        bookingRequest.setBooker(user);
        bookingRequest.setStatus(BookingStatusType.WAITING);

        Booking savedBooking = bookingStorage.save(bookingRequest);
        log.info("Обработан запрос на новое бронирование от пользователя ID {}, по запросу: \n {}",
                userId, bookingRequestDto);
        return BookingMapper.bookingToBookingDto(savedBooking);
    }

    @Override
    public BookingDto ownerChangeStatus(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено по ID " + bookingId));

        if (!booking.getItem().getOwner().equals(userId)) {
            throw new PermissionException("У вас недостаточно прав для изменения статуса");
        }

        if (booking.getStatus().equals(BookingStatusType.APPROVED)) {
            throw new StatusException("Вы не можете изменить уже одобренный статус");
        }

        if (approved.equals(true)) {
            booking.setStatus(BookingStatusType.APPROVED);
        } else if (approved.equals(false)) {
            booking.setStatus(BookingStatusType.REJECTED);
        }

        Booking savedBooking = bookingStorage.save(booking);
        return BookingMapper.bookingToBookingDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено по ID " + bookingId));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().equals(userId)) {
            throw new PermissionException("У вас недостаточно прав для получения бронирования по ID " + booking);
        }
        return BookingMapper.bookingToBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingByUserId(Long userId, String state) {
        if (!userStorage.existsById(userId)) {
            throw new UserNotFoundException("Пользователь не найден по ID " + userId);
        }

        checkState(state);

        List<Booking> bookings;

        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingStorage.findAllByBookerId(userId);
                return bookings.stream().map(BookingMapper::bookingToBookingDto).collect(Collectors.toList());
            case "CURRENT":
                bookings = bookingStorage.findAllCurrentBookings(userId, LocalDateTime.now());
                return bookings.stream().map(BookingMapper::bookingToBookingDto).collect(Collectors.toList());
            case "PAST":
                bookings = bookingStorage.findAllPastBookings(userId, LocalDateTime.now());
                return bookings.stream().map(BookingMapper::bookingToBookingDto).collect(Collectors.toList());
            case "FUTURE":
                bookings = bookingStorage.findAllFutureBookings(userId);
                return bookings.stream().map(BookingMapper::bookingToBookingDto).collect(Collectors.toList());
            case "WAITING":
                bookings = bookingStorage.findAllByBookerAndStatus(userId, BookingStatusType.WAITING);
                return bookings.stream().map(BookingMapper::bookingToBookingDto).collect(Collectors.toList());
            case "REJECTED":
                bookings = bookingStorage.findAllByBookerAndStatus(userId, BookingStatusType.REJECTED);
                return bookings.stream().map(BookingMapper::bookingToBookingDto).collect(Collectors.toList());
            default:
                return Collections.emptyList();

        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        if (!userStorage.existsById(userId)) {
            throw new UserNotFoundException("Пользователь не найден по ID " + userId);
        }

        checkState(state);

        List<Booking> bookings;

        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingStorage.findAllOwnerItemBookings(userId);
                return bookings.stream().map(BookingMapper::bookingToBookingDto).collect(Collectors.toList());
            case "CURRENT":
                bookings = bookingStorage.findAllOwnerCurrentBookings(userId, LocalDateTime.now());
                return bookings.stream().map(BookingMapper::bookingToBookingDto).collect(Collectors.toList());
            case "PAST":
                bookings = bookingStorage.findAllPastOwnerItemBookings(userId, LocalDateTime.now());
                return bookings.stream().map(BookingMapper::bookingToBookingDto).collect(Collectors.toList());
            case "FUTURE":
                bookings = bookingStorage.findAllFutureOwnerItemBookings(userId, LocalDateTime.now());
                return bookings.stream().map(BookingMapper::bookingToBookingDto).collect(Collectors.toList());
            case "WAITING":
                bookings = bookingStorage.findAllOwnerItemBookedByStatus(userId, BookingStatusType.WAITING);
                return bookings.stream().map(BookingMapper::bookingToBookingDto).collect(Collectors.toList());
            case "REJECTED":
                bookings = bookingStorage.findAllOwnerItemBookedByStatus(userId, BookingStatusType.REJECTED);
                return bookings.stream().map(BookingMapper::bookingToBookingDto).collect(Collectors.toList());
            default:
                return Collections.emptyList();

        }
    }

    private void checkState(String state) {
        try {
            BookingStateType.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException exp) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    private void checkAvailableItem(final Item item) {
        if (!item.getAvailable()) {
            throw new ItemNotAvailableForBookingException("Предмет не доступен для бронирования по ID " + item.getId());
        }
    }

    private void checkBooking(final BookingRequestDto bookingRequestDto) {
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
