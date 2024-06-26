package ru.practicum.shareit.integrtest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.request.model.ItemRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mvc;

    private ItemWithBookingDto makeDefaultItemWithBookingDto() {
        return ItemWithBookingDto.builder()
                .id(1L)
                .owner(1L)
                .name("TEST NAME")
                .description("TEST DESCRIPTION")
                .available(true)
                .request(makeDefaultItemRequest())
                .lastBooking(new BookingShortDto(1L, 2L))
                .nextBooking(new BookingShortDto(2L, 3L))
                .comments(List.of(makeDefaultCommentDto()))
                .build();
    }

    private ItemRequest makeDefaultItemRequest() {
        return ItemRequest.builder()
                .id(1L)
                .requestorId(1L)
                .description("TEST DESCRIPTION ITEM REQUEST")
                .created(LocalDateTime.now())
                .build();
    }

    private ItemDto makeDefaultItemDto() {
        return ItemDto.builder()
                .id(1L)
                .available(true)
                .description("TEST ITEM DESC")
                .name("TEST ITEM NAME")
                .owner(1L)
                .build();
    }

    private CommentDto makeDefaultCommentDto() {
        return CommentDto.builder()
                .text("TEST COMMENT TEXT")
                .created(LocalDateTime.now())
                .authorName("TEST NAME")
                .build();
    }

    private static final String URL = "http://localhost:8080/items";

    @Test
    void testGetItemById_ShouldReturnItemWithBooking_WhenItemExists() throws Exception {
        // given
        Long itemId = 1L;
        ItemWithBookingDto itemWithBookingDto = makeDefaultItemWithBookingDto();

        when(itemService.getItemByIdWithBooking(anyLong(), anyLong()))
                .thenReturn(itemWithBookingDto);
        // expect
        mvc.perform(get(URL.concat("/{itemId}"), itemId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.owner", is(1)))
                .andExpect(jsonPath("$.name", is(itemWithBookingDto.getName())))
                .andExpect(jsonPath("$.description", is(itemWithBookingDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemWithBookingDto.getAvailable())))
                .andExpect(jsonPath("$.request", notNullValue()))
                .andExpect(jsonPath("$.lastBooking", notNullValue()))
                .andExpect(jsonPath("$.nextBooking", notNullValue()))
                .andExpect(jsonPath("$.comments", notNullValue()))
                .andExpect(jsonPath("$.comments", hasSize(1)));
    }

    @Test
    void testGetItemById_ShouldReturnError_WhenItemIdIncorrect() throws Exception {
        // given
        Long itemId = -1L;

        when(itemService.getItemByIdWithBooking(anyLong(), anyLong()))
                .thenThrow(new ItemNotFoundException("Предмет не найден по ID " + itemId));
        // expect
        mvc.perform(get(URL.concat("/{itemId}"), itemId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetOwnerItems_ShouldReturnListItemWithBookingsByItemOwnerId_WhenItemExists() throws Exception {
        // given
        ItemWithBookingDto itemWithBookingDto = makeDefaultItemWithBookingDto();

        when(itemService.getOwnerItemsWithBookings(anyLong(), isA(PageRequest.class)))
                .thenReturn(List.of(itemWithBookingDto));
        // expect
        mvc.perform(get(URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("from", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemWithBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].owner", is(1)))
                .andExpect(jsonPath("$.[0].name", is(itemWithBookingDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemWithBookingDto.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemWithBookingDto.getAvailable())))
                .andExpect(jsonPath("$.[0].request", notNullValue()))
                .andExpect(jsonPath("$.[0].lastBooking", notNullValue()))
                .andExpect(jsonPath("$.[0].nextBooking", notNullValue()))
                .andExpect(jsonPath("$.[0].comments", notNullValue()))
                .andExpect(jsonPath("$.[0].comments", hasSize(1)));
    }

    @Test
    void testFindItemsByText_ShouldReturnListItemDtoByRegEx_WhenItemExists() throws Exception {
        // given
        String regEx = "TEST ITEM NAME";
        ItemDto itemDto = makeDefaultItemDto();

        when(itemService.findItemsByText(anyString(), isA(PageRequest.class)))
                .thenReturn(List.of(itemDto));
        // expect
        mvc.perform(get(URL.concat("/search"))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("text", regEx)
                        .param("from", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].owner", is(1)))
                .andExpect(jsonPath("$.[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.[0].requestId", nullValue()));
    }

    @Test
    void testAddNewItem_ShouldSaveNewItem_WhenItemNotExists() throws Exception {
        // given
        ItemDto itemDto = makeDefaultItemDto();
        ItemRegisterDto itemRegisterDto = ItemRegisterDto.builder()
                .owner(1L)
                .name("TEST ITEM NAME")
                .description("TEST ITEM DESC")
                .available(true)
                .build();

        when(itemService.addNewItem(anyLong(), isA(ItemRegisterDto.class)))
                .thenReturn(itemDto);
        // expect
        mvc.perform(post(URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemRegisterDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.owner", is(1)))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", nullValue()));
    }

    @Test
    void testAddNewItem_ShouldReturnError_WhenItemRegisterDtoDescriptionIsNull() throws Exception {
        // given
        ItemDto itemDto = makeDefaultItemDto();
        ItemRegisterDto itemRegisterDto = ItemRegisterDto.builder()
                .owner(1L)
                .name("TEST ITEM NAME")
                .available(true)
                .build();

        when(itemService.addNewItem(anyLong(), isA(ItemRegisterDto.class)))
                .thenReturn(itemDto);
        // expect
        mvc.perform(post(URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemRegisterDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddNewItem_ShouldReturnError_WhenItemRegisterDtoNameIsNull() throws Exception {
        // given
        ItemDto itemDto = makeDefaultItemDto();
        ItemRegisterDto itemRegisterDto = ItemRegisterDto.builder()
                .owner(1L)
                .description("TEST ITEM DESC")
                .available(true)
                .build();

        when(itemService.addNewItem(anyLong(), isA(ItemRegisterDto.class)))
                .thenReturn(itemDto);
        // expect
        mvc.perform(post(URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemRegisterDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddNewItem_ShouldReturnError_WhenItemRegisterDtoAvailableIsNull() throws Exception {
        // given
        ItemDto itemDto = makeDefaultItemDto();
        ItemRegisterDto itemRegisterDto = ItemRegisterDto.builder()
                .owner(1L)
                .name("TEST ITEM NAME")
                .description("TEST ITEM DESC")
                .build();

        when(itemService.addNewItem(anyLong(), isA(ItemRegisterDto.class)))
                .thenReturn(itemDto);
        // expect
        mvc.perform(post(URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemRegisterDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateItem_ShouldReturnUpdatedItemDto_WhenItemExists() throws Exception {
        // given
        ItemDto itemDto = makeDefaultItemDto();

        when(itemService.updateItem(anyLong(), anyLong(), isA(ItemDto.class)))
                .thenReturn(itemDto);
        // expect
        mvc.perform(patch(URL.concat("/{itemId}"), itemDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.owner", is(1)))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", nullValue()));
    }

    @Test
    void testUpdateItem_ShouldReturnError_WhenItemNotExists() throws Exception {
        // given
        ItemDto itemDto = makeDefaultItemDto();

        when(itemService.updateItem(anyLong(), anyLong(), isA(ItemDto.class)))
                .thenThrow(new ItemNotFoundException("Предмет не найден по ID " + itemDto.getId()));
        // expect
        mvc.perform(patch(URL.concat("/{itemId}"), itemDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteItem_ShouldDeleteItemById_WhenItemExists() throws Exception {
        // given
        ItemDto itemDto = makeDefaultItemDto();

        // expect
        mvc.perform(delete(URL.concat("/{itemId}"), itemDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testAddNewComment_ShouldReturnSavedComment_WhenTextIsNotNull() throws Exception {
        // given
        ItemDto itemDto = makeDefaultItemDto();
        CommentDto commentDto = makeDefaultCommentDto();

        when(itemService.addNewComment(anyLong(), anyLong(), isA(CommentAddDto.class)))
                .thenReturn(commentDto);
        // expect
        mvc.perform(post(URL.concat("/{itemId}/comment"), itemDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(new CommentAddDto("TEST COMMENT TEXT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created", notNullValue()));
    }

    @Test
    void testAddNewComment_ShouldReturnError_WhenTextIsNull() throws Exception {
        // given
        ItemDto itemDto = makeDefaultItemDto();
        CommentDto commentDto = makeDefaultCommentDto();

        when(itemService.addNewComment(anyLong(), anyLong(), isA(CommentAddDto.class)))
                .thenReturn(commentDto);
        // expect
        mvc.perform(post(URL.concat("/{itemId}/comment"), itemDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(new CommentAddDto()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}