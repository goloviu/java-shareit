package ru.practicum.shareit.integrtest.services;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatusType;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(properties = "db.name=test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplIntegrTest {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;

    private User user;
    private Item item;
    private Item item2;
    private Booking booking;
    private Booking booking2;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .name("Test name")
                .email("teseemail@email.ru")
                .build();

        userRepository.save(user);

        item = Item.builder()
                .owner(user.getId())
                .name("ITEM TEST NAME")
                .description("ITEM TEST DESCRIPTION")
                .available(true)
                .build();

        item2 = Item.builder()
                .owner(user.getId())
                .name("ITEM TEST NAME2")
                .description("ITEM TEST DESCRIPTION2")
                .available(true)
                .build();

        itemRepository.save(item);
        itemRepository.save(item2);

        booking = Booking.builder()
                .status(BookingStatusType.APPROVED)
                .end(LocalDateTime.now().plusHours(5))
                .start(LocalDateTime.now().plusHours(1))
                .booker(user)
                .item(item)
                .build();

        booking2 = Booking.builder()
                .status(BookingStatusType.APPROVED)
                .end(LocalDateTime.now().plusHours(5))
                .start(LocalDateTime.now().plusHours(1))
                .booker(user)
                .item(item)
                .build();

        bookingRepository.save(booking);
        bookingRepository.save(booking2);
    }

    @AfterEach
    public void tearDown() {
        // Release test data after each test method
        bookingRepository.deleteById(booking.getId());
        bookingRepository.deleteById(booking2.getId());
        itemRepository.deleteById(item.getId());
        itemRepository.deleteById(item2.getId());
        userRepository.deleteById(user.getId());
    }

    @Test
    void testGetOwnerBookings_ShouldReturnListBookingDto_WhenBookingsExists() {
        // do
        List<BookingDto> result = bookingService.getOwnerBookings(user.getId(), "ALL", PageRequest.of(0, 3));

        // expect
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getStart(), notNullValue());
        assertThat(result.get(0).getEnd(), notNullValue());
        assertThat(result.get(0).getBooker(), equalTo(user));
        assertThat(result.get(0).getItem(), equalTo(item));
        assertThat(result.get(0).getStatus(), equalTo(BookingStatusType.APPROVED));
        assertThat(result.get(1).getStart(), notNullValue());
        assertThat(result.get(1).getEnd(), notNullValue());
        assertThat(result.get(1).getBooker(), equalTo(user));
        assertThat(result.get(1).getItem(), equalTo(item));
        assertThat(result.get(1).getStatus(), equalTo(BookingStatusType.APPROVED));
    }
}