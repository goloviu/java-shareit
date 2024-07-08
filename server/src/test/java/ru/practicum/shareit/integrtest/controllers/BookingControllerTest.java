package ru.practicum.shareit.integrtest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatusType;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exceptions.BookingNotFoundException;
import ru.practicum.shareit.exceptions.StatusException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    private static final String URL = "http://localhost:8080/bookings";

    @Test
    void testBookingAddNewRequest_ShouldReturnNewBooking_WhenBookingRequestDtoIsNotNull() throws Exception {
        // given
        Long userId = 1L;

        BookingRequestDto requestDto = new BookingRequestDto(1L, LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(5));
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .item(new Item())
                .end(requestDto.getEnd().toString())
                .start(requestDto.getStart().toString())
                .status(BookingStatusType.WAITING)
                .booker(new User())
                .build();

        when(bookingService.addNewRequest(userId, requestDto))
                .thenReturn(bookingDto);

        // expect
        mvc.perform(post(URL)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.booker", is(new User()), User.class))
                .andExpect(jsonPath("$.item", is(new Item()), Item.class))
                .andExpect(jsonPath("$.status", is(BookingStatusType.WAITING.toString())));
    }

    @Test
    void testOwnerChangeStatus_ReturnBookingDto_WhenBookingIsExists() throws Exception {
        // given
        Long userId = 1L;
        Boolean approved = true;

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .item(new Item())
                .end(LocalDateTime.now().plusHours(5).toString())
                .start(LocalDateTime.now().plusHours(2).toString())
                .status(BookingStatusType.APPROVED)
                .booker(new User())
                .build();

        when(bookingService.ownerChangeStatus(userId, bookingDto.getId(), approved))
                .thenReturn(bookingDto);

        // expect
        mvc.perform(patch(URL.concat("/{bookingId}"), bookingDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.booker", is(new User()), User.class))
                .andExpect(jsonPath("$.item", is(new Item()), Item.class))
                .andExpect(jsonPath("$.status", is(BookingStatusType.APPROVED.toString())));
    }

    @Test
    void testOwnerChangeStatus_ReturnError_WhenBookingIsNotExists() throws Exception {
        // given
        Long userId = 1L;
        Long bookingId = 1L;
        Boolean approved = true;

        when(bookingService.ownerChangeStatus(userId, bookingId, approved))
                .thenThrow(new BookingNotFoundException("Бронирование не найдено"));

        // expect
        mvc.perform(patch(URL.concat("/{bookingId}"), bookingId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testOwnerChangeStatus_ReturnError_WhenUserIsNotExists() throws Exception {
        // given
        Long userId = 1L;
        Long bookingId = 1L;
        Boolean approved = true;

        when(bookingService.ownerChangeStatus(userId, bookingId, approved))
                .thenThrow(new UserNotFoundException("Пользователь не найден"));

        // expect
        mvc.perform(patch(URL.concat("/{bookingId}"), bookingId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testOwnerChangeStatus_ReturnError_WhenApprovedIsNull() throws Exception {
        // given
        Long bookingId = 1L;

        when(bookingService.ownerChangeStatus(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new StatusException("Неверное значение approved"));

        // expect
        mvc.perform(patch(URL.concat("/{bookingId}"), bookingId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetBookingById_ShouldReturnBookingDtoByBookingId_WhenBookingIsExists() throws Exception {
        // given
        Long userId = 1L;

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .item(new Item())
                .end(LocalDateTime.now().plusHours(5).toString())
                .start(LocalDateTime.now().plusHours(2).toString())
                .status(BookingStatusType.APPROVED)
                .booker(new User())
                .build();

        when(bookingService.getBookingById(userId, bookingDto.getId()))
                .thenReturn(bookingDto);

        // expect
        mvc.perform(get(URL.concat("/{bookingId}"), bookingDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .param("bookingId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.booker", is(new User()), User.class))
                .andExpect(jsonPath("$.item", is(new Item()), Item.class))
                .andExpect(jsonPath("$.status", is(BookingStatusType.APPROVED.toString())));
    }

    @Test
    void testGetBookingById_ShouldReturnErorr_WhenBookingIsNotExists() throws Exception {
        // given
        Long userId = 1L;
        Long bookingId = 1L;

        when(bookingService.getBookingById(userId, bookingId))
                .thenThrow(new BookingNotFoundException("Бронирование не найдено"));

        // expect
        mvc.perform(get(URL.concat("/{bookingId}"), bookingId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .param("bookingId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetBookingByUserId_ShouldReturnBookingListByUserIdAndState_WhenBookingIsExists() throws Exception {
        // given
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .item(new Item())
                .end(LocalDateTime.now().plusHours(5).toString())
                .start(LocalDateTime.now().plusHours(2).toString())
                .status(BookingStatusType.APPROVED)
                .booker(new User())
                .build();

        when(bookingService.getBookingByUserId(anyLong(), isA(String.class), isA(PageRequest.class)))
                .thenReturn(List.of(bookingDto));

        // expect
        mvc.perform(get(URL, bookingDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "1")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", notNullValue()))
                .andExpect(jsonPath("$.[0].end", notNullValue()))
                .andExpect(jsonPath("$.[0].booker", is(new User()), User.class))
                .andExpect(jsonPath("$.[0].item", is(new Item()), Item.class))
                .andExpect(jsonPath("$.[0].status", is(BookingStatusType.APPROVED.toString())));
    }

    @Test
    void testGetOwnerBookings_ShouldReturnListBookingsByItemOwnerIdAndState_WhenBookingIsExists() throws Exception {
        // given
        Item item = new Item();
        item.setOwner(1L);

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .item(new Item())
                .end(LocalDateTime.now().plusHours(5).toString())
                .start(LocalDateTime.now().plusHours(2).toString())
                .status(BookingStatusType.APPROVED)
                .booker(new User())
                .build();

        when(bookingService.getOwnerBookings(anyLong(), isA(String.class), isA(PageRequest.class)))
                .thenReturn(List.of(bookingDto));

        // expect
        mvc.perform(get(URL.concat("/owner"), item.getOwner())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "1")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", notNullValue()))
                .andExpect(jsonPath("$.[0].end", notNullValue()))
                .andExpect(jsonPath("$.[0].booker", is(new User()), User.class))
                .andExpect(jsonPath("$.[0].item", is(new Item()), Item.class))
                .andExpect(jsonPath("$.[0].status", is(BookingStatusType.APPROVED.toString())));
    }
}