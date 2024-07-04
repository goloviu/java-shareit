package ru.practicum.shareit.test.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingServiceImpl;
import ru.practicum.shareit.booking.BookingStatusType;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingStorage;
    @Mock
    private ItemRepository itemStorage;
    @Mock
    private UserRepository userStorage;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        this.bookingService = new BookingServiceImpl(bookingStorage, itemStorage, userStorage);
    }

    private User makeDefaultUser() {
        return User.builder()
                .id(1L)
                .email("testemail@test.test")
                .name("USER TEST NAME")
                .build();
    }

    private Item makeDefaultItem() {
        return Item.builder()
                .id(1L)
                .owner(2L)
                .name("ITEM TEST NAME")
                .description("ITEM TEST DESCRIPTION")
                .available(true)
                .build();
    }

    private Booking makeDefaultBooking() {
        BookingRequestDto bookingDto = makeDefaultBookingRequestDto();
        return Booking.builder()
                .id(1L)
                .status(BookingStatusType.WAITING)
                .end(bookingDto.getEnd())
                .start(bookingDto.getStart())
                .booker(makeDefaultUser())
                .item(makeDefaultItem())
                .build();
    }

    private BookingRequestDto makeDefaultBookingRequestDto() {
        return new BookingRequestDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));
    }

    @Test
    void testAddNewRequest_ShouldReturnError_WhenBookingRequestDtoStartDateNull() {
        // given
        BookingRequestDto bookingRequestDto = makeDefaultBookingRequestDto();
        bookingRequestDto.setStart(null);

        // expect
        final DateTimeBookingException exception = Assertions.assertThrows(
                DateTimeBookingException.class,
                () -> bookingService.addNewRequest(1L, bookingRequestDto));

        assertThat(exception.getMessage(),
                equalTo("Значения начала бронирования и конца бронирования должны быть указаны"));
    }

    @Test
    void testAddNewRequest_ShouldReturnError_WhenBookingRequestDtoEndDateNull() {
        // given
        BookingRequestDto bookingRequestDto = makeDefaultBookingRequestDto();
        bookingRequestDto.setEnd(null);

        // expect
        final DateTimeBookingException exception = Assertions.assertThrows(
                DateTimeBookingException.class,
                () -> bookingService.addNewRequest(1L, bookingRequestDto));

        assertThat(exception.getMessage(),
                equalTo("Значения начала бронирования и конца бронирования должны быть указаны"));
    }

    @Test
    void testAddNewRequest_ShouldReturnError_WhenBookingRequestDtoEndDateAndStartDateSame() {
        // given
        BookingRequestDto bookingRequestDto = makeDefaultBookingRequestDto();
        LocalDateTime plusFiveHourseFromNow = LocalDateTime.now().plusHours(5);
        bookingRequestDto.setEnd(plusFiveHourseFromNow);
        bookingRequestDto.setStart(plusFiveHourseFromNow);

        // expect
        final DateTimeBookingException exception = Assertions.assertThrows(
                DateTimeBookingException.class,
                () -> bookingService.addNewRequest(1L, bookingRequestDto));

        assertThat(exception.getMessage(),
                equalTo("Дата и время начала и конца бронирования не должны быть равны"));
    }

    @Test
    void testAddNewRequest_ShouldReturnError_WhenBookingRequestDtoStartDateIsAfterEndDate() {
        // given
        BookingRequestDto bookingRequestDto = makeDefaultBookingRequestDto();
        bookingRequestDto.setEnd(LocalDateTime.now().plusHours(1));
        bookingRequestDto.setStart(LocalDateTime.now().plusHours(5));

        // expect
        final DateTimeBookingException exception = Assertions.assertThrows(
                DateTimeBookingException.class,
                () -> bookingService.addNewRequest(1L, bookingRequestDto));

        assertThat(exception.getMessage(),
                equalTo("Дата и время начала бронирования не может быть после окончания: "
                        + bookingRequestDto.getStart()));
    }

    @Test
    void testAddNewRequest_ShouldReturnError_WhenBookingRequestDtoStartDateIsBeforeNow() {
        // given
        BookingRequestDto bookingRequestDto = makeDefaultBookingRequestDto();
        bookingRequestDto.setEnd(LocalDateTime.now().plusHours(1));
        bookingRequestDto.setStart(LocalDateTime.now().minusDays(1));

        // expect
        final DateTimeBookingException exception = Assertions.assertThrows(
                DateTimeBookingException.class,
                () -> bookingService.addNewRequest(1L, bookingRequestDto));

        assertThat(exception.getMessage(),
                equalTo("Дата и время бронирования не может быть в прошлом: "
                        + bookingRequestDto.getStart()));
    }

    @Test
    void testAddNewRequest_ShouldReturnError_WhenItemIsNull() {
        // given
        BookingRequestDto bookingRequestDto = makeDefaultBookingRequestDto();
        bookingRequestDto.setEnd(LocalDateTime.now().plusHours(5));
        bookingRequestDto.setStart(LocalDateTime.now().plusHours(1));
        Long itemId = 1L;
        when(itemStorage.findById(anyLong()))
                .thenThrow(new ItemNotFoundException("Не найден предмет по ID " + itemId));

        // expect
        final ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> bookingService.addNewRequest(1L, bookingRequestDto));

        verify(itemStorage, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(itemStorage);
        assertThat(exception.getMessage(), equalTo("Не найден предмет по ID " + itemId));
    }

    @Test
    void testAddNewRequest_ShouldReturnError_WhenUserIdEqualsItemOwnerId() {
        // given
        BookingRequestDto bookingRequestDto = makeDefaultBookingRequestDto();
        Long userId = 1L;

        Item item = Item.builder()
                .id(1L)
                .owner(1L)
                .name("ITEM TEST NAME")
                .description("ITEM TEST DESCRIPTION")
                .available(true)
                .build();

        when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));

        // expect
        final PermissionException exception = Assertions.assertThrows(
                PermissionException.class,
                () -> bookingService.addNewRequest(userId, bookingRequestDto));

        verify(itemStorage, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(itemStorage);
        assertThat(exception.getMessage(),
                equalTo("Пользователь не может запросить бронирование у себя. Пользователь ID " + userId +
                        " владелец предмета ID " + item.getOwner()));
    }

    @Test
    void testAddNewRequest_ShouldReturnError_WhenUserIsNull() {
        // given
        BookingRequestDto bookingRequestDto = makeDefaultBookingRequestDto();
        Long userId = 1L;

        Item item = makeDefaultItem();

        when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(userStorage.findById(anyLong()))
                .thenThrow(new UserNotFoundException("Пользователь не найден по ID " + userId));

        // expect
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookingService.addNewRequest(userId, bookingRequestDto));

        verify(itemStorage, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(itemStorage, userStorage);
        assertThat(exception.getMessage(),
                equalTo("Пользователь не найден по ID " + userId));
    }

    @Test
    void testAddNewRequest_ShouldReturnError_WhenItemAvailableIsFalse() {
        // given
        BookingRequestDto bookingRequestDto = makeDefaultBookingRequestDto();
        User user = makeDefaultUser();
        Item item = makeDefaultItem();
        item.setAvailable(false);

        when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));

        // expect
        final ItemNotAvailableForBookingException exception = Assertions.assertThrows(
                ItemNotAvailableForBookingException.class,
                () -> bookingService.addNewRequest(user.getId(), bookingRequestDto));

        verify(itemStorage, times(1))
                .findById(anyLong());
        verify(userStorage, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(itemStorage, userStorage);
        assertThat(exception.getMessage(),
                equalTo("Предмет не доступен для бронирования по ID " + item.getId()));
    }

    @Test
    void testAddNewRequest_ShouldSaveNewBooking_WhenBookingIsNotNull() {
        // given
        BookingRequestDto bookingRequestDto = makeDefaultBookingRequestDto();
        User user = makeDefaultUser();
        Item item = makeDefaultItem();

        Booking booking = Booking.builder()
                .id(1L)
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .build();

        when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingStorage.save(isA(Booking.class)))
                .thenReturn(booking);

        // do
        BookingDto result = bookingService.addNewRequest(user.getId(), bookingRequestDto);
        BookingDto expect = BookingMapper.bookingToBookingDto(booking);

        // expect
        verify(itemStorage, times(1))
                .findById(anyLong());
        verify(userStorage, times(1))
                .findById(anyLong());
        verify(bookingStorage, times(1))
                .save(isA(Booking.class));
        verifyNoMoreInteractions(itemStorage, userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testOwnerChangeStatus_ShouldReturnError_WhenBookingIsNotExists() {
        // given
        Long bookingId = 1L;
        Long userId = 1L;

        when(bookingStorage.findById(anyLong()))
                .thenThrow(new BookingNotFoundException("Бронирование не найдено по ID " + bookingId));

        // expect
        final BookingNotFoundException exception = Assertions.assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.ownerChangeStatus(userId, bookingId, true));

        verify(bookingStorage, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(bookingStorage);
        assertThat(exception.getMessage(), equalTo("Бронирование не найдено по ID " + bookingId));
    }

    @Test
    void testOwnerChangeStatus_ShouldReturnError_WhenItemOwnerIdNotEqualsToUserId() {
        // given
        Long userId = 1L;
        Booking booking = makeDefaultBooking();

        when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // expect
        final PermissionException exception = Assertions.assertThrows(
                PermissionException.class,
                () -> bookingService.ownerChangeStatus(userId, booking.getId(), false));

        verify(bookingStorage, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(bookingStorage);
        assertThat(exception.getMessage(), equalTo("У вас недостаточно прав для изменения статуса"));
    }

    @Test
    void testOwnerChangeStatus_ShouldReturnError_WhenBookingStatusIsApproved() {
        // given
        Long userId = 2L;
        Booking booking = makeDefaultBooking();
        booking.setStatus(BookingStatusType.APPROVED);

        when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // expect
        final StatusException exception = Assertions.assertThrows(
                StatusException.class,
                () -> bookingService.ownerChangeStatus(userId, booking.getId(), false));

        verify(bookingStorage, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(bookingStorage);
        assertThat(exception.getMessage(), equalTo("Вы не можете изменить уже одобренный статус"));
    }

    @Test
    void testOwnerChangeStatus_ShouldReturnBookingStatusApproved_WhenArgumentApprovedTrue() {
        // given
        Long userId = 2L;
        Booking booking = makeDefaultBooking();

        when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(bookingStorage.save(booking))
                .thenReturn(booking);

        // do
        BookingDto result = bookingService.ownerChangeStatus(userId, booking.getId(), true);
        BookingStatusType expect = BookingStatusType.APPROVED;

        // expect
        assertThat(expect, equalTo(result.getStatus()));
        verify(bookingStorage, times(1))
                .findById(anyLong());
        verify(bookingStorage, times(1))
                .save(isA(Booking.class));
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void testOwnerChangeStatus_ShouldReturnBookingStatusRejected_WhenArgumentApprovedFalse() {
        // given
        Long userId = 2L;
        Booking booking = makeDefaultBooking();

        when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(bookingStorage.save(booking))
                .thenReturn(booking);

        // do
        BookingDto result = bookingService.ownerChangeStatus(userId, booking.getId(), false);
        BookingStatusType expect = BookingStatusType.REJECTED;

        // expect
        assertThat(expect, equalTo(result.getStatus()));
        verify(bookingStorage, times(1))
                .findById(anyLong());
        verify(bookingStorage, times(1))
                .save(isA(Booking.class));
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void testGetBookingById_ShouldReturnError_WhenBookingIsNull() {
        // given
        Long bookingId = 1L;
        Long userId = 1L;

        when(bookingStorage.findById(anyLong()))
                .thenThrow(new BookingNotFoundException("Бронирование не найдено по ID " + bookingId));

        // expect
        final BookingNotFoundException exception = Assertions.assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getBookingById(userId, bookingId));

        verify(bookingStorage, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(bookingStorage);
        assertThat(exception.getMessage(), equalTo("Бронирование не найдено по ID " + bookingId));
    }

    @Test
    void testGetBookingById_ShouldReturnError_WhenUserNotBookerAndNotItemOwner() {
        // given
        Long userId = 3L;
        Booking booking = makeDefaultBooking();

        when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // expect
        final PermissionException exception = Assertions.assertThrows(
                PermissionException.class,
                () -> bookingService.getBookingById(userId, booking.getId()));

        verify(bookingStorage, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(bookingStorage);
        assertThat(exception.getMessage(),
                equalTo("У вас недостаточно прав для получения бронирования по ID " + booking.getId()));
    }

    @Test
    void testGetBookingById_ShouldReturnBookingById_WhenUserAreBooker() {
        // given
        Long userId = 1L;
        Booking booking = makeDefaultBooking();

        when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // do
        BookingDto result = bookingService.getBookingById(userId, booking.getId());
        BookingDto expect = BookingMapper.bookingToBookingDto(booking);

        // expect
        assertThat(expect, equalTo(result));
        verify(bookingStorage, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void testGetBookingById_ShouldReturnBookingById_WhenUserAreItemOwner() {
        // given
        Long userId = 2L;
        Booking booking = makeDefaultBooking();

        when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // do
        BookingDto result = bookingService.getBookingById(userId, booking.getId());
        BookingDto expect = BookingMapper.bookingToBookingDto(booking);

        // expect
        assertThat(expect, equalTo(result));
        verify(bookingStorage, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void testGetOwnerBookings_ShouldReturnError_WhenUserIsNotExists() {
        // given
        Long userId = 1L;

        when(userStorage.existsById(1L))
                .thenThrow(new UserNotFoundException("Пользователь не найден по ID " + userId));

        // expect
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getOwnerBookings(userId, "ALL", PageRequest.of(0, 1)));

        verify(userStorage, times(1))
                .existsById(1L);
        verifyNoMoreInteractions(userStorage);
        assertThat(exception.getMessage(), equalTo("Пользователь не найден по ID " + userId));
    }

    @Test
    void testGetOwnerBookings_ShouldReturnError_WhenStateIsUnknown() {
        // given
        Long userId = 1L;
        String state = "UNKNOWN";

        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // expect
        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.getOwnerBookings(userId, state, PageRequest.of(0, 1)));

        verify(userStorage, times(1))
                .existsById(anyLong());
        verifyNoMoreInteractions(userStorage);
        assertThat(exception.getMessage(), equalTo("Unknown state: " + state));
    }

    @Test
    void testGetOwnerBookings_ShouldReturnAllBookingsByItemOwnerId_WhenStateIsAll() {
        // given
        PageRequest page = PageRequest.of(0, 1);
        String state = "ALL";
        Long userId = 2L;
        Booking booking = makeDefaultBooking();

        when(bookingStorage.findAllOwnerItemBookings(userId, page))
                .thenReturn(List.of(booking));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // do
        List<BookingDto> result = bookingService.getOwnerBookings(userId, state, page);

        BookingDto bookingDto = BookingMapper.bookingToBookingDto(booking);
        List<BookingDto> expect = List.of(bookingDto);

        // expect
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(bookingStorage, times(1))
                .findAllOwnerItemBookings(anyLong(), isA(PageRequest.class));
        verifyNoMoreInteractions(userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
        assertThat(booking.getItem().getOwner(), equalTo(userId));
    }

    @Test
    void testGetOwnerBookings_ShouldReturnAllBookingsByItemOwnerId_WhenStateIsCurrent() {
        // given
        PageRequest page = PageRequest.of(0, 1);
        String state = "CURRENT";
        Long userId = 2L;
        Booking booking = makeDefaultBooking();

        when(bookingStorage.findAllOwnerCurrentBookings(anyLong(), isA(LocalDateTime.class), isA(Pageable.class)))
                .thenReturn(List.of(booking));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // do
        List<BookingDto> result = bookingService.getOwnerBookings(userId, state, page);

        BookingDto bookingDto = BookingMapper.bookingToBookingDto(booking);
        List<BookingDto> expect = List.of(bookingDto);

        // expect
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(bookingStorage, times(1))
                .findAllOwnerCurrentBookings(anyLong(), isA(LocalDateTime.class), isA(Pageable.class));
        verifyNoMoreInteractions(userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
        assertThat(booking.getItem().getOwner(), equalTo(userId));
    }

    @Test
    void testGetOwnerBookings_ShouldReturnPastBookingsByOwnerId_WhenStateIsPast() {
        // given
        PageRequest page = PageRequest.of(0, 1);
        String state = "PAST";
        Long userId = 2L;
        Booking booking = makeDefaultBooking();
        booking.setEnd(LocalDateTime.now().minusHours(5));

        when(bookingStorage.findAllPastOwnerItemBookings(anyLong(), isA(LocalDateTime.class), isA(PageRequest.class)))
                .thenReturn(List.of(booking));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // do
        List<BookingDto> result = bookingService.getOwnerBookings(userId, state, page);

        BookingDto bookingDto = BookingMapper.bookingToBookingDto(booking);
        List<BookingDto> expect = List.of(bookingDto);

        // expect
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(bookingStorage, times(1))
                .findAllPastOwnerItemBookings(anyLong(), isA(LocalDateTime.class), isA(PageRequest.class));
        verifyNoMoreInteractions(userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
        assertThat(booking.getEnd().isBefore(LocalDateTime.now()), equalTo(true));
        assertThat(booking.getItem().getOwner(), equalTo(userId));
    }

    @Test
    void testGetOwnerBookings_ShouldReturnFutureBookingsByItemOwnerId_WhenStateIsFuture() {
        // given
        PageRequest page = PageRequest.of(0, 1);
        String state = "FUTURE";
        Long userId = 2L;
        Booking booking = makeDefaultBooking();
        booking.setStart(LocalDateTime.now().plusHours(5));

        when(bookingStorage.findAllFutureOwnerItemBookings(anyLong(), isA(LocalDateTime.class), isA(PageRequest.class)))
                .thenReturn(List.of(booking));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // do
        List<BookingDto> result = bookingService.getOwnerBookings(userId, state, page);

        BookingDto bookingDto = BookingMapper.bookingToBookingDto(booking);
        List<BookingDto> expect = List.of(bookingDto);

        // expect
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(bookingStorage, times(1))
                .findAllFutureOwnerItemBookings(anyLong(), isA(LocalDateTime.class), isA(PageRequest.class));
        verifyNoMoreInteractions(userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
        assertThat(booking.getStart().isAfter(LocalDateTime.now()), equalTo(true));
        assertThat(booking.getItem().getOwner(), equalTo(userId));
    }

    @Test
    void testGetOwnerBookings_ShouldReturnWaitingBookingsByItemOwnerId_WhenStateIsWaiting() {
        // given
        PageRequest page = PageRequest.of(0, 1);
        String state = "WAITING";
        Long userId = 2L;
        Booking booking = makeDefaultBooking();
        booking.setStatus(BookingStatusType.WAITING);

        when(bookingStorage.findAllOwnerItemBookedByStatus(userId, BookingStatusType.WAITING, PageRequest.of(0, 1)))
                .thenReturn(List.of(booking));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // do
        List<BookingDto> result = bookingService.getOwnerBookings(userId, state, page);

        BookingDto bookingDto = BookingMapper.bookingToBookingDto(booking);
        List<BookingDto> expect = List.of(bookingDto);

        // expect
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(bookingStorage, times(1))
                .findAllOwnerItemBookedByStatus(userId, BookingStatusType.WAITING, PageRequest.of(0, 1));
        verifyNoMoreInteractions(userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
        assertThat(booking.getStatus(), equalTo(BookingStatusType.WAITING));
        assertThat(booking.getItem().getOwner(), equalTo(userId));
    }

    @Test
    void testGetOwnerBookings_ShouldReturnRejectedBookingsByItemOwnerId_WhenStateIsReject() {
        // given
        PageRequest page = PageRequest.of(0, 1);
        String state = "REJECTED";
        Long userId = 2L;
        Booking booking = makeDefaultBooking();
        booking.setStatus(BookingStatusType.REJECTED);

        when(bookingStorage.findAllOwnerItemBookedByStatus(userId, BookingStatusType.REJECTED, PageRequest.of(0, 1)))
                .thenReturn(List.of(booking));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // do
        List<BookingDto> result = bookingService.getOwnerBookings(userId, state, page);

        BookingDto bookingDto = BookingMapper.bookingToBookingDto(booking);
        List<BookingDto> expect = List.of(bookingDto);

        // expect
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(bookingStorage, times(1))
                .findAllOwnerItemBookedByStatus(userId, BookingStatusType.REJECTED, PageRequest.of(0, 1));
        verifyNoMoreInteractions(userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
        assertThat(booking.getStatus(), equalTo(BookingStatusType.REJECTED));
        assertThat(booking.getItem().getOwner(), equalTo(userId));
    }

    @Test
    void testGetBookingByUserId_ShouldReturnError_WhenUserIsNotExists() {
        // given
        Long userId = 1L;

        when(userStorage.existsById(1L))
                .thenThrow(new UserNotFoundException("Пользователь не найден по ID " + userId));

        // expect
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getBookingByUserId(userId, "ALL", PageRequest.of(0, 1)));

        verify(userStorage, times(1))
                .existsById(1L);
        verifyNoMoreInteractions(userStorage);
        assertThat(exception.getMessage(), equalTo("Пользователь не найден по ID " + userId));
    }

    @Test
    void testGetBookingByUserId_ShouldReturnError_WhenStateIsUnknown() {
        // given
        Long userId = 1L;
        String state = "UNKNOWN";

        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // expect
        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.getBookingByUserId(userId, state, PageRequest.of(0, 1)));

        verify(userStorage, times(1))
                .existsById(anyLong());
        verifyNoMoreInteractions(userStorage);
        assertThat(exception.getMessage(), equalTo("Unknown state: " + state));
    }

    @Test
    void testGetBookingByUserId_ShouldReturnAllBookings_WhenStateIsAll() {
        // given
        PageRequest page = PageRequest.of(0, 1);
        String state = "ALL";
        Long userId = 1L;
        Booking booking = makeDefaultBooking();

        when(bookingStorage.findAllByBookerId(userId, page))
                .thenReturn(List.of(booking));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // do
        List<BookingDto> result = bookingService.getBookingByUserId(userId, state, page);

        BookingDto bookingDto = BookingMapper.bookingToBookingDto(booking);
        List<BookingDto> expect = List.of(bookingDto);

        // expect
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(bookingStorage, times(1))
                .findAllByBookerId(anyLong(), isA(PageRequest.class));
        verifyNoMoreInteractions(userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testGetBookingByUserId_ShouldReturnPastBookings_WhenStateIsPast() {
        // given
        PageRequest page = PageRequest.of(0, 1);
        String state = "PAST";
        Long userId = 1L;
        Booking booking = makeDefaultBooking();
        booking.setEnd(LocalDateTime.now().minusHours(5));

        when(bookingStorage.findAllPastBookings(anyLong(), isA(LocalDateTime.class), isA(PageRequest.class)))
                .thenReturn(List.of(booking));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // do
        List<BookingDto> result = bookingService.getBookingByUserId(userId, state, page);

        BookingDto bookingDto = BookingMapper.bookingToBookingDto(booking);
        List<BookingDto> expect = List.of(bookingDto);

        // expect
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(bookingStorage, times(1))
                .findAllPastBookings(anyLong(), isA(LocalDateTime.class), isA(PageRequest.class));
        verifyNoMoreInteractions(userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
        assertThat(booking.getEnd().isBefore(LocalDateTime.now()), equalTo(true));
    }

    @Test
    void testGetBookingByUserId_ShouldReturnCurrentBookings_WhenStateIsCurrent() {
        // given
        PageRequest page = PageRequest.of(0, 1);
        String state = "CURRENT";
        Long userId = 2L;
        Booking booking = makeDefaultBooking();

        when(bookingStorage.findAllCurrentBookings(anyLong(), isA(LocalDateTime.class), isA(Pageable.class)))
                .thenReturn(List.of(booking));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // do
        List<BookingDto> result = bookingService.getBookingByUserId(userId, state, page);

        BookingDto bookingDto = BookingMapper.bookingToBookingDto(booking);
        List<BookingDto> expect = List.of(bookingDto);

        // expect
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(bookingStorage, times(1))
                .findAllCurrentBookings(anyLong(), isA(LocalDateTime.class), isA(Pageable.class));
        verifyNoMoreInteractions(userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
        assertThat(booking.getItem().getOwner(), equalTo(userId));
    }

    @Test
    void testGetBookingByUserId_ShouldReturnFutureBookings_WhenStateIsFuture() {
        // given
        PageRequest page = PageRequest.of(0, 1);
        String state = "FUTURE";
        Long userId = 1L;
        Booking booking = makeDefaultBooking();
        booking.setStart(LocalDateTime.now().plusHours(5));

        when(bookingStorage.findAllFutureBookings(anyLong(), isA(PageRequest.class)))
                .thenReturn(List.of(booking));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // do
        List<BookingDto> result = bookingService.getBookingByUserId(userId, state, page);

        BookingDto bookingDto = BookingMapper.bookingToBookingDto(booking);
        List<BookingDto> expect = List.of(bookingDto);

        // expect
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(bookingStorage, times(1))
                .findAllFutureBookings(anyLong(), isA(PageRequest.class));
        verifyNoMoreInteractions(userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
        assertThat(booking.getStart().isAfter(LocalDateTime.now()), equalTo(true));
    }

    @Test
    void testGetBookingByUserId_ShouldReturnWaitingBookings_WhenStateIsWaiting() {
        // given
        PageRequest page = PageRequest.of(0, 1);
        String state = "WAITING";
        Long userId = 1L;
        Booking booking = makeDefaultBooking();
        booking.setStatus(BookingStatusType.WAITING);

        when(bookingStorage.findAllByBookerAndStatus(userId, BookingStatusType.WAITING, PageRequest.of(0, 1)))
                .thenReturn(List.of(booking));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // do
        List<BookingDto> result = bookingService.getBookingByUserId(userId, state, page);

        BookingDto bookingDto = BookingMapper.bookingToBookingDto(booking);
        List<BookingDto> expect = List.of(bookingDto);

        // expect
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(bookingStorage, times(1))
                .findAllByBookerAndStatus(userId, BookingStatusType.WAITING, PageRequest.of(0, 1));
        verifyNoMoreInteractions(userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
        assertThat(booking.getStatus(), equalTo(BookingStatusType.WAITING));
    }

    @Test
    void testGetBookingByUserId_ShouldReturnRejectedBookings_WhenStateIsReject() {
        // given
        PageRequest page = PageRequest.of(0, 1);
        String state = "REJECTED";
        Long userId = 1L;
        Booking booking = makeDefaultBooking();
        booking.setStatus(BookingStatusType.REJECTED);

        when(bookingStorage.findAllByBookerAndStatus(userId, BookingStatusType.REJECTED, PageRequest.of(0, 1)))
                .thenReturn(List.of(booking));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        // do
        List<BookingDto> result = bookingService.getBookingByUserId(userId, state, page);

        BookingDto bookingDto = BookingMapper.bookingToBookingDto(booking);
        List<BookingDto> expect = List.of(bookingDto);

        // expect
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(bookingStorage, times(1))
                .findAllByBookerAndStatus(userId, BookingStatusType.REJECTED, PageRequest.of(0, 1));
        verifyNoMoreInteractions(userStorage, bookingStorage);
        assertThat(result, equalTo(expect));
        assertThat(booking.getStatus(), equalTo(BookingStatusType.REJECTED));
    }
}