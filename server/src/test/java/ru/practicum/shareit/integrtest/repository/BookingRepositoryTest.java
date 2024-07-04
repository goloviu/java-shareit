package ru.practicum.shareit.integrtest.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatusType;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@DataJpaTest
class BookingRepositoryTest {

    private BookingRepository bookingRepository;
    private UserRepository userRepository;
    private ItemRepository itemRepository;

    private User testUser;
    private User testUser2;
    private Item item;
    private Booking booking; // bookerId 2, itemOwnerId 1

    @Autowired
    public BookingRepositoryTest(BookingRepository bookingRepository, UserRepository userRepository, ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .name("Test name")
                .email("teseemail@email.ru")
                .build();

        testUser2 = User.builder()
                .name("Test name")
                .email("teseemail2@email.ru")
                .build();

        userRepository.save(testUser);
        userRepository.save(testUser2);

        item = Item.builder()
                .owner(testUser.getId())
                .name("ITEM TEST NAME")
                .description("ITEM TEST DESCRIPTION")
                .available(true)
                .build();

        itemRepository.save(item);

        booking = Booking.builder()
                .status(BookingStatusType.WAITING)
                .end(LocalDateTime.now().plusHours(5))
                .start(LocalDateTime.now().plusHours(1))
                .booker(testUser2)
                .item(item)
                .build();

        bookingRepository.save(booking);
    }

    @AfterEach
    public void tearDown() {
        // Release test data after each test method
        userRepository.delete(testUser);
        userRepository.delete(testUser2);
        itemRepository.delete(item);
        bookingRepository.delete(booking);
    }

    @Test
    void testFindAllByBookerId_ShouldReturnBookingListByBookerId_WhenBookingIsExists() {
        // given
        Long bookerId = booking.getBooker().getId();

        // do
        List<Booking> result = bookingRepository.findAllByBookerId(bookerId, PageRequest.of(0, 1));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(booking)));
    }

    @Test
    void testFindAllCurrentBookings_ShouldReturnCurrentBookingListByBookerId_WhenBookingIsExistsAndStartedAndNotEnded() {
        // given
        Long bookerId = booking.getBooker().getId();
        booking.setStart(LocalDateTime.now().minusHours(2));

        // do
        List<Booking> result = bookingRepository.findAllCurrentBookings(bookerId, LocalDateTime.now(),
                PageRequest.of(0, 1));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(booking)));
    }

    @Test
    void testFindAllPastBookings_ShouldReturnListPastBookingsByBookerIdOrItemOwnerId_WhenBookingIsExists() {
        // given
        Long bookerId = booking.getBooker().getId();
        booking.setEnd(LocalDateTime.now().minusHours(2));

        // do
        List<Booking> result = bookingRepository.findAllPastBookings(bookerId, LocalDateTime.now(),
                PageRequest.of(0, 1));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(booking)));
    }

    @Test
    void testFindAllFutureBookings_ShouldReturnFutureBookingsListByBookerIdOrItemOwnerId_WhenBookingIsExists() {
        // given
        Long bookerId = booking.getBooker().getId();
        booking.setStart(LocalDateTime.now().plusHours(2));

        // do
        List<Booking> result = bookingRepository.findAllFutureBookings(bookerId, PageRequest.of(0, 1));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(booking)));
    }

    @Test
    void testFindAllByBookerAndStatus_ShouldReturnBookingsListByBookerIdAndStatus_WhenBookingIsExists() {
        // given
        Long bookerId = booking.getBooker().getId();

        // do
        List<Booking> result = bookingRepository.findAllByBookerAndStatus(bookerId, BookingStatusType.WAITING,
                PageRequest.of(0, 1));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(booking)));
    }

    @Test
    void testFindAllOwnerItemBookings_ShouldReturnBookingsListByItemOwnerId_WhenBookingIsExists() {
        // given
        Long itemOwnerId = booking.getItem().getOwner();

        // do
        List<Booking> result = bookingRepository.findAllOwnerItemBookings(itemOwnerId, PageRequest.of(0, 1));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(booking)));
    }

    @Test
    void testFindAllOwnerCurrentBookings_ShouldReturnCurrentBookingsListByItemOwnerId_WhenBookingIsExists() {
        // given
        Long itemOwnerId = booking.getItem().getOwner();
        booking.setStart(LocalDateTime.now().minusHours(2));

        // do
        List<Booking> result = bookingRepository.findAllOwnerCurrentBookings(itemOwnerId, LocalDateTime.now(),
                PageRequest.of(0, 1));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(booking)));
    }

    @Test
    void testFindAllPastOwnerItemBookings_ShouldReturnPastBookingsListByItemOwnerId_WhenBookingIsExists() {
        // given
        Long itemOwnerId = booking.getItem().getOwner();
        booking.setEnd(LocalDateTime.now().minusHours(2));

        // do
        List<Booking> result = bookingRepository.findAllPastOwnerItemBookings(itemOwnerId, LocalDateTime.now(),
                PageRequest.of(0, 1));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(booking)));
    }

    @Test
    void testFindAllFutureOwnerItemBookings_ShouldReturnFutureBookingsListByOwnerId_WhenBookingIsExists() {
        // given
        Long itemOwnerId = booking.getItem().getOwner();
        booking.setStart(LocalDateTime.now().plusHours(2));

        // do
        List<Booking> result = bookingRepository.findAllFutureOwnerItemBookings(itemOwnerId, LocalDateTime.now(),
                PageRequest.of(0, 1));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(booking)));
    }

    @Test
    void testFindAllOwnerItemBookedByStatus_ShouldReturnBookingListByItemOwnerIdAndStatus_WhenBookingIsExists() {
        // given
        Long itemOwnerId = booking.getItem().getOwner();

        // do
        List<Booking> result = bookingRepository.findAllOwnerItemBookedByStatus(itemOwnerId, BookingStatusType.WAITING,
                PageRequest.of(0, 1));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(booking)));
    }

    @Test
    void testFindFirstByItemId_ShouldReturnFirstBookingByItemId_WhenBookingExistsAndItemExitst() {
        // given
        Long itemId = item.getId();

        // do
        Optional<Booking> result = bookingRepository.findFirstByItemId(itemId);
        boolean expect = true;

        // expect
        assertThat(result.isPresent(), equalTo(expect));
        assertThat(result.get(), equalTo(booking));
    }

    @Test
    void testFindLastBooking_ShouldReturnLastBookingByItemId_WhenBookingExistsAndItemExitst() {
        // given
        Long itemId = item.getId();
        booking.setStart(LocalDateTime.now().minusHours(2));
        booking.setStatus(BookingStatusType.APPROVED);

        // do
        List<Booking> result = bookingRepository.findLastBooking(itemId, LocalDateTime.now());

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(booking)));
    }

    @Test
    void testFindNextBooking_ShouldReturnNextApprovedBookingByItemId_WhenBookingExistsAndItemExitst() {
        // given
        Long itemId = item.getId();
        booking.setStart(LocalDateTime.now().plusHours(2));
        booking.setStatus(BookingStatusType.APPROVED);

        // do
        List<Booking> result = bookingRepository.findNextBooking(itemId, LocalDateTime.now());

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(booking)));
    }
}