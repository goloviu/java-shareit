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
import ru.practicum.shareit.booking.BookingStatusType;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
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
class ItemServiceImplIntegrTest {

    private final ItemService itemService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;

    private User user;
    private Item item;
    private Item item2;
    private Booking booking;

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

        bookingRepository.save(booking);
    }

    @AfterEach
    public void tearDown() {
        // Release test data after each test method
        bookingRepository.deleteById(booking.getId());
        itemRepository.deleteById(item.getId());
        itemRepository.deleteById(item2.getId());
        userRepository.deleteById(user.getId());
    }

    @Test
    void testGetOwnerItemsWithBookings_ShouldReturnListItemWithBookingsByItemOwnerId_WhenBookingExists() {
        // do
        List<ItemWithBookingDto> result = itemService.getOwnerItemsWithBookings(user.getId(), PageRequest.of(0, 3));

        // expect
        BookingShortDto expectNextBooking = new BookingShortDto(booking.getId(), booking.getBooker().getId());

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getName(), equalTo(item.getName()));
        assertThat(result.get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(result.get(0).getOwner(), equalTo(user.getId()));
        assertThat(result.get(0).getAvailable(), equalTo(true));
        assertThat(result.get(0).getLastBooking(), nullValue());
        assertThat(result.get(0).getNextBooking(), equalTo(expectNextBooking));
        assertThat(result.get(1).getName(), equalTo(item2.getName()));
        assertThat(result.get(1).getDescription(), equalTo(item2.getDescription()));
        assertThat(result.get(1).getOwner(), equalTo(user.getId()));
        assertThat(result.get(1).getAvailable(), equalTo(true));
        assertThat(result.get(1).getLastBooking(), nullValue());
        assertThat(result.get(1).getNextBooking(), nullValue());
    }
}