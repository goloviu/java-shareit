package ru.practicum.shareit.integrtest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswerDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private static final String URL = "http://localhost:8080/requests";

    @Test
    void testMakeRequest_ShouldReturnCorrectItemRequestDto_WhenCorrectRequest() throws Exception {
        // given
        Long userId = 1L;

        NewItemRequestDto newRequestDto = new NewItemRequestDto("Test desc");

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Test Desc")
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.addNewRequest(userId, newRequestDto))
                .thenReturn(requestDto);

        // expect
        mvc.perform(post(URL)
                        .content(mapper.writeValueAsString(newRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.created", notNullValue()));
    }

    @Test
    void testGetOwnUserItemRequests_ShouldReturnOwnRequests_WhenRequestIsNotNull() throws Exception {
        // given
        Long userId = 1L;

        ItemRequestWithAnswerDto requestDto = ItemRequestWithAnswerDto.builder()
                .id(1L)
                .description("Test Desc")
                .items(List.of(new ItemForRequestDto()))
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getOwnUserItemRequests(userId))
                .thenReturn(List.of(requestDto));

        // expect
        mvc.perform(get(URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.[0].created", notNullValue()))
                .andExpect(jsonPath("$.[0].items", hasSize(1)));
    }

    @Test
    void testGetOwnUserItemRequests_ShouldReturnError_WhenUserIdNotExists() throws Exception {
        // given
        Long userId = 1L;

        when(itemRequestService.getOwnUserItemRequests(userId))
                .thenThrow(new UserNotFoundException("Пользователь не найден"));

        // expect
        mvc.perform(get(URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUsersRequests_ShouldReturnOtherUsersRequests_WhenRequestsExists() throws Exception {
        // given
        Long userId = 1L;

        ItemRequestWithAnswerDto requestDto = ItemRequestWithAnswerDto.builder()
                .id(1L)
                .description("Test Desc")
                .items(List.of(new ItemForRequestDto()))
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getUsersItemRequests(anyLong(), isA(PageRequest.class)))
                .thenReturn(List.of(requestDto));

        // expect
        mvc.perform(get(URL.concat("/all"))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("from", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.[0].created", notNullValue()))
                .andExpect(jsonPath("$.[0].items", hasSize(1)));
    }

    @Test
    void testGetRequestById_ShouldReturnRequestById_WhenRequestExists() throws Exception {
        // given
        Long requestId = 1L;

        ItemRequestWithAnswerDto requestDto = ItemRequestWithAnswerDto.builder()
                .id(1L)
                .description("Test Desc")
                .items(List.of(new ItemForRequestDto()))
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(requestDto);

        // expect
        mvc.perform(get(URL.concat("/{requestId}"), requestId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("from", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.created", notNullValue()))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void testGetRequestById_ShouldReturnError_WhenRequestNotExists() throws Exception {
        // given
        Long requestId = 1L;

        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(new ItemRequestNotFoundException("Запрос не найден"));

        // expect
        mvc.perform(get(URL.concat("/{requestId}"), requestId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("from", "1")
                        .param("size", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetRequestById_ShouldReturnError_WhenUserNotExists() throws Exception {
        // given
        Long requestId = 1L;

        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(new UserNotFoundException("Пользователь не найден"));

        // expect
        mvc.perform(get(URL.concat("/{requestId}"), requestId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("from", "1")
                        .param("size", "1"))
                .andExpect(status().isNotFound());
    }
}