package ru.practicum.shareit.test.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatusType;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemStorage;
    @Mock
    private UserRepository userStorage;
    @Mock
    private BookingRepository bookingStorage;
    @Mock
    private CommentRepository commentStorage;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        this.itemService = new ItemServiceImpl(itemStorage, userStorage, bookingStorage, commentStorage);
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
        return Booking.builder()
                .id(1L)
                .status(BookingStatusType.WAITING)
                .end(LocalDateTime.now().plusHours(5))
                .start(LocalDateTime.now().plusHours(2))
                .booker(makeDefaultUser())
                .item(makeDefaultItem())
                .build();
    }

    private Comment makeDefaultComment() {
        return Comment.builder()
                .id(1L)
                .text("Test Comment Text")
                .item(makeDefaultItem())
                .author(makeDefaultUser())
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetOwnerItemsWithBookings_ShouldReturnListItemWithBookingsByItemOwnerId_WhenBookingExists() {
        // given
        Item item = makeDefaultItem();
        item.setOwner(1L);

        User user = makeDefaultUser();

        Comment comment = makeDefaultComment();

        Booking lastBooking = Booking.builder()
                .id(2L)
                .item(item)
                .booker(user)
                .status(BookingStatusType.APPROVED)
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        Booking nextBooking = Booking.builder()
                .id(2L)
                .item(item)
                .booker(user)
                .status(BookingStatusType.APPROVED)
                .start(LocalDateTime.now().minusHours(5))
                .end(LocalDateTime.now().plusHours(6))
                .build();

        when(itemStorage.findByOwner(anyLong(), isA(PageRequest.class)))
                .thenReturn(List.of(item));
        when(bookingStorage.findLastBooking(anyLong(), isA(LocalDateTime.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingStorage.findNextBooking(anyLong(), isA(LocalDateTime.class)))
                .thenReturn(List.of(nextBooking));
        when(commentStorage.findByItemId(anyLong()))
                .thenReturn(List.of(comment));

        // do
        List<ItemWithBookingDto> result = itemService.getOwnerItemsWithBookings(user.getId(), PageRequest.of(0, 2));
        ItemWithBookingDto expect = ItemMapper.itemToItemWithBookingDto(item, lastBooking, nextBooking);
        expect.setComments(List.of(ItemMapper.commentToCommentDto(comment)));

        // expect
        verify(itemStorage, times(1))
                .findByOwner(anyLong(), isA(PageRequest.class));
        verify(bookingStorage, times(1))
                .findNextBooking(anyLong(), isA(LocalDateTime.class));
        verify(bookingStorage, times(1))
                .findLastBooking(anyLong(), isA(LocalDateTime.class));
        verify(commentStorage, times(1))
                .findByItemId(anyLong());
        verifyNoMoreInteractions(itemStorage, bookingStorage, commentStorage);
        assertThat(result.get(0), equalTo(expect));
    }

    @Test
    void testGetItemByIdWithBooking_ShouldReturnItemWithBookingsByItemId_WhenItemExists() {
        // given
        Item item = makeDefaultItem();
        item.setOwner(1L);

        User user = makeDefaultUser();

        Comment comment = makeDefaultComment();

        Booking lastBooking = Booking.builder()
                .id(2L)
                .item(item)
                .booker(user)
                .status(BookingStatusType.APPROVED)
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        Booking nextBooking = Booking.builder()
                .id(2L)
                .item(item)
                .booker(user)
                .status(BookingStatusType.APPROVED)
                .start(LocalDateTime.now().minusHours(5))
                .end(LocalDateTime.now().plusHours(6))
                .build();

        when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(bookingStorage.findLastBooking(anyLong(), isA(LocalDateTime.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingStorage.findNextBooking(anyLong(), isA(LocalDateTime.class)))
                .thenReturn(List.of(nextBooking));
        when(commentStorage.findByItemId(anyLong()))
                .thenReturn(List.of(comment));

        // do
        ItemWithBookingDto result = itemService.getItemByIdWithBooking(user.getId(), item.getId());
        ItemWithBookingDto expect = ItemMapper.itemToItemWithBookingDto(item, lastBooking, nextBooking);
        expect.setComments(List.of(ItemMapper.commentToCommentDto(comment)));

        // expect
        verify(itemStorage, times(1))
                .findById(anyLong());
        verify(bookingStorage, times(1))
                .findNextBooking(anyLong(), isA(LocalDateTime.class));
        verify(bookingStorage, times(1))
                .findLastBooking(anyLong(), isA(LocalDateTime.class));
        verify(commentStorage, times(1))
                .findByItemId(anyLong());
        verifyNoMoreInteractions(itemStorage, bookingStorage, commentStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testGetItemByIdWithBooking_ShouldReturnItemWithBookingsByItemId_WhenItemExistsAndExitsOnlyLastBooking() {
        // given
        Item item = makeDefaultItem();
        item.setOwner(1L);

        User user = makeDefaultUser();

        Comment comment = makeDefaultComment();

        Booking lastBooking = Booking.builder()
                .id(2L)
                .item(item)
                .booker(user)
                .status(BookingStatusType.APPROVED)
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(bookingStorage.findLastBooking(anyLong(), isA(LocalDateTime.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingStorage.findNextBooking(anyLong(), isA(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(commentStorage.findByItemId(anyLong()))
                .thenReturn(List.of(comment));

        // do
        ItemWithBookingDto result = itemService.getItemByIdWithBooking(user.getId(), item.getId());
        ItemWithBookingDto expect = ItemMapper.itemToItemWithBookingDto(item);
        expect.setLastBooking(new BookingShortDto(lastBooking.getId(), lastBooking.getBooker().getId()));
        expect.setComments(List.of(ItemMapper.commentToCommentDto(comment)));

        // expect
        verify(itemStorage, times(1))
                .findById(anyLong());
        verify(bookingStorage, times(1))
                .findNextBooking(anyLong(), isA(LocalDateTime.class));
        verify(bookingStorage, times(1))
                .findLastBooking(anyLong(), isA(LocalDateTime.class));
        verify(commentStorage, times(1))
                .findByItemId(anyLong());
        verifyNoMoreInteractions(itemStorage, bookingStorage, commentStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testGetItemByIdWithBooking_ShouldReturnItemWithoutBookingsByItemId_WhenItemExistsAndUserNotOwner() {
        // given
        Item item = makeDefaultItem();
        User user = makeDefaultUser();

        Comment comment = makeDefaultComment();

        when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(commentStorage.findByItemId(anyLong()))
                .thenReturn(List.of(comment));

        // do
        ItemWithBookingDto result = itemService.getItemByIdWithBooking(user.getId(), item.getId());
        ItemWithBookingDto expect = ItemMapper.itemToItemWithBookingDto(item);
        expect.setComments(List.of(ItemMapper.commentToCommentDto(comment)));

        // expect
        verify(itemStorage, times(1))
                .findById(anyLong());
        verify(commentStorage, times(1))
                .findByItemId(anyLong());
        verifyNoMoreInteractions(itemStorage, commentStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testGetItemByIdWithBooking_ShouldReturnError_WhenItemNotExists() {
        // given
        Long itemId = 1L;
        when(itemStorage.findById(anyLong()))
                .thenThrow(new ItemNotFoundException("Предмет не найден по ID " + itemId));

        // expect
        final ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> itemService.getItemByIdWithBooking(1L, itemId));

        assertThat(exception.getMessage(),
                equalTo("Предмет не найден по ID " + itemId));
    }

    @Test
    void testFindItemsByText_ShouldReturnListItemDtoByRegEx_WhenItemExists() {
        // given
        Item item = makeDefaultItem();
        String regEx = "ITEM TEST NAME";

        when(itemStorage.findAvailableItemsByText(regEx, PageRequest.of(0, 1)))
                .thenReturn(List.of(item));

        // do
        List<ItemDto> result = itemService.findItemsByText(regEx, PageRequest.of(0, 1));
        List<ItemDto> expect = List.of(ItemMapper.itemToItemDto(item));

        // expect
        verify(itemStorage, times(1))
                .findAvailableItemsByText(anyString(), isA(PageRequest.class));
        verifyNoMoreInteractions(itemStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testFindItemsByText_ShouldReturnEmptyList_WhenRegExIsNull() {
        // given
        Item item = makeDefaultItem();
        String regEx = null;

        // do
        List<ItemDto> result = itemService.findItemsByText(regEx, PageRequest.of(0, 1));
        List<ItemDto> expect = List.of(ItemMapper.itemToItemDto(item));

        // expect
        verifyNoMoreInteractions(itemStorage);
        assertThat(result, hasSize(0));
        assertThat(result, not(expect));
    }

    @Test
    void testFindItemsByText_ShouldReturnEmptyList_WhenRegExIsEmpty() {
        // given
        Item item = makeDefaultItem();
        String regEx = "";

        // do
        List<ItemDto> result = itemService.findItemsByText(regEx, PageRequest.of(0, 1));
        List<ItemDto> expect = List.of(ItemMapper.itemToItemDto(item));

        // expect
        verifyNoMoreInteractions(itemStorage);
        assertThat(result, hasSize(0));
        assertThat(result, not(expect));
    }

    @Test
    void testAddNewItem_ShouldSaveNewItem_WhenItemIsNotNull() {
        // given
        Item item = makeDefaultItem();
        User user = makeDefaultUser();
        user.setId(2L);
        ItemRegisterDto itemRegisterDto = ItemRegisterDto.builder()
                .name("ITEM TEST NAME")
                .description("ITEM TEST DESCRIPTION")
                .available(true)
                .build();

        when(itemStorage.saveItem(item, itemRegisterDto.getRequestId()))
                .thenReturn(item);
        when(itemStorage.exists(isA(Example.class)))
                .thenReturn(false);
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);

        // do
        ItemDto result = itemService.addNewItem(user.getId(), itemRegisterDto);
        ItemDto expect = ItemMapper.itemToItemDto(item);

        // expect
        verify(itemStorage, times(1))
                .saveItem(item, itemRegisterDto.getRequestId());
        verify(itemStorage, times(1))
                .exists(isA(Example.class));
        verify(userStorage, times(1))
                .existsById(anyLong());
        verifyNoMoreInteractions(itemStorage, userStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testAddNewItem_ShouldReturnError_WhenUserNotExists() {
        // given
        Long userId = 1L;

        when(userStorage.existsById(anyLong()))
                .thenReturn(false);

        // expect
        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> itemService.addNewItem(userId, null));

        assertThat(exception.getMessage(),
                equalTo("Пользователя по ID " + userId + " не существует"));
    }

    @Test
    void testAddNewItem_ShouldReturnError_WhenItemExists() {
        // given
        Long userId = 1L;
        ItemRegisterDto itemRegisterDto = ItemRegisterDto.builder()
                .name("ITEM TEST NAME")
                .description("ITEM TEST DESCRIPTION")
                .available(true)
                .build();

        when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        when(itemStorage.exists(isA(Example.class)))
                .thenReturn(true);
        // expect
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemService.addNewItem(userId, itemRegisterDto));

        assertThat(exception.getMessage(),
                equalTo("Вещь уже существует"));
    }

    @Test
    void testUpdateItem_ShouldUpdateItemName_WhenItemExists() {
        // given
        Item item = makeDefaultItem();
        Long userId = 2L;
        ItemDto itemDto = ItemDto.builder()
                .name("UPDATED ITEM TEST NAME")
                .build();

        when(itemStorage.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemStorage.save(isA(Item.class)))
                .thenReturn(item);
        // do
        ItemDto result = itemService.updateItem(userId, item.getId(), itemDto);
        ItemDto expect = ItemMapper.itemToItemDto(item);

        // expect
        verify(itemStorage, times(1))
                .findById(anyLong());
        verify(itemStorage, times(1))
                .save(isA(Item.class));
        verifyNoMoreInteractions(itemStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testUpdateItem_ShouldUpdateItemDescription_WhenItemExists() {
        // given
        Item item = makeDefaultItem();
        Long userId = 2L;
        ItemDto itemDto = ItemDto.builder()
                .description("UPDATE ITEM DESCRIPTION")
                .build();

        when(itemStorage.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemStorage.save(isA(Item.class)))
                .thenReturn(item);
        // do
        ItemDto result = itemService.updateItem(userId, item.getId(), itemDto);
        ItemDto expect = ItemMapper.itemToItemDto(item);

        // expect
        verify(itemStorage, times(1))
                .findById(anyLong());
        verify(itemStorage, times(1))
                .save(isA(Item.class));
        verifyNoMoreInteractions(itemStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testUpdateItem_ShouldUpdateItemAvailable_WhenItemExists() {
        // given
        Item item = makeDefaultItem();
        Long userId = 2L;
        ItemDto itemDto = ItemDto.builder()
                .available(false)
                .build();

        when(itemStorage.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemStorage.save(isA(Item.class)))
                .thenReturn(item);
        // do
        ItemDto result = itemService.updateItem(userId, item.getId(), itemDto);
        ItemDto expect = ItemMapper.itemToItemDto(item);

        // expect
        verify(itemStorage, times(1))
                .findById(anyLong());
        verify(itemStorage, times(1))
                .save(isA(Item.class));
        verifyNoMoreInteractions(itemStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testUpdateItem_ShouldUpdateItemNameAndItemDescriptionAndItemAvailable_WhenItemExists() {
        // given
        Item item = makeDefaultItem();
        Long userId = 2L;
        ItemDto itemDto = ItemDto.builder()
                .name("UPDATED ITEM TEST NAME")
                .description("UPDATED ITEM TEST DESCRIPTION")
                .available(false)
                .build();

        when(itemStorage.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemStorage.save(isA(Item.class)))
                .thenReturn(item);
        // do
        ItemDto result = itemService.updateItem(userId, item.getId(), itemDto);
        ItemDto expect = ItemMapper.itemToItemDto(item);

        // expect
        verify(itemStorage, times(1))
                .findById(anyLong());
        verify(itemStorage, times(1))
                .save(isA(Item.class));
        verifyNoMoreInteractions(itemStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testUpdateItem_ShouldUpdateItemNameAndItemDescription_WhenItemExists() {
        // given
        Item item = makeDefaultItem();
        Long userId = 2L;
        ItemDto itemDto = ItemDto.builder()
                .name("UPDATED ITEM TEST NAME")
                .description("UPDATED ITEM TEST DESCRIPTION")
                .build();

        when(itemStorage.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemStorage.save(isA(Item.class)))
                .thenReturn(item);
        // do
        ItemDto result = itemService.updateItem(userId, item.getId(), itemDto);
        ItemDto expect = ItemMapper.itemToItemDto(item);

        // expect
        verify(itemStorage, times(1))
                .findById(anyLong());
        verify(itemStorage, times(1))
                .save(isA(Item.class));
        verifyNoMoreInteractions(itemStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testUpdateItem_ShouldUpdateItemNameAndItemAvailable_WhenItemExists() {
        // given
        Item item = makeDefaultItem();
        Long userId = 2L;
        ItemDto itemDto = ItemDto.builder()
                .name("UPDATED ITEM TEST NAME")
                .available(true)
                .build();

        when(itemStorage.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemStorage.save(isA(Item.class)))
                .thenReturn(item);
        // do
        ItemDto result = itemService.updateItem(userId, item.getId(), itemDto);
        ItemDto expect = ItemMapper.itemToItemDto(item);

        // expect
        verify(itemStorage, times(1))
                .findById(anyLong());
        verify(itemStorage, times(1))
                .save(isA(Item.class));
        verifyNoMoreInteractions(itemStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testUpdateItem_ShouldUpdateItemDescriptionAndItemAvailable_WhenItemExists() {
        // given
        Item item = makeDefaultItem();
        Long userId = 2L;
        ItemDto itemDto = ItemDto.builder()
                .description("UPDATED ITEM TEST DESCRIPTION")
                .available(true)
                .build();

        when(itemStorage.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemStorage.save(isA(Item.class)))
                .thenReturn(item);
        // do
        ItemDto result = itemService.updateItem(userId, item.getId(), itemDto);
        ItemDto expect = ItemMapper.itemToItemDto(item);

        // expect
        verify(itemStorage, times(1))
                .findById(anyLong());
        verify(itemStorage, times(1))
                .save(isA(Item.class));
        verifyNoMoreInteractions(itemStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testUpdateItem_ShouldReturnError_WhenItemNotExists() {
        // given
        Item item = makeDefaultItem();
        Long userId = 2L;
        ItemDto itemDto = ItemDto.builder()
                .name("UPDATED ITEM TEST NAME")
                .description("UPDATED ITEM TEST DESCRIPTION")
                .available(false)
                .build();

        when(itemStorage.findById(anyLong()))
                .thenThrow(new ItemNotFoundException("Предмет не найден по ID " + item.getId()));
        // expect
        final ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> itemService.updateItem(userId, item.getId(), itemDto));

        assertThat(exception.getMessage(),
                equalTo("Предмет не найден по ID " + item.getId()));
    }

    @Test
    void testUpdateItem_ShouldReturnError_WhenUserAreNotOwner() {
        // given
        Item item = makeDefaultItem();
        Long userId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("UPDATED ITEM TEST NAME")
                .description("UPDATED ITEM TEST DESCRIPTION")
                .available(false)
                .build();

        when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        // expect
        final PermissionException exception = assertThrows(
                PermissionException.class,
                () -> itemService.updateItem(userId, item.getId(), itemDto));

        assertThat(exception.getMessage(),
                equalTo("Недостаточно прав для изменения вещи у пользователя ID " + userId + " " +
                        "изменить вещь может только владелец"));
    }

    @Test
    void testDeleteItem_ShouldDeleteItemByIdAndUserId_WhenItemExists() {
        // given
        Long itemId = 1L;
        Long userId = 1L;

        // do
        itemService.deleteItem(userId, itemId);

        // expect
        verify(itemStorage, times(1))
                .removeByIdAndOwner(userId, itemId);
        verifyNoMoreInteractions(itemStorage);
    }

    @Test
    void testAddNewComment_ShouldAddNewComment_WhenUserAreBookerAndBookingHaveBeenEnded() {
        // given
        Item item = makeDefaultItem();
        Booking booking = makeDefaultBooking();
        booking.setEnd(LocalDateTime.now().minusHours(1));
        Comment comment = makeDefaultComment();
        Long userId = 1L;

        when(commentStorage.save(isA(Comment.class)))
                .thenReturn(comment);
        when(bookingStorage.findFirstByItemId(anyLong()))
                .thenReturn(Optional.of(booking));
        // do
        CommentDto result = itemService.addNewComment(userId, item.getId(), new CommentAddDto("Test Comment Text"));
        comment.setCreated(result.getCreated()); // Присваиваем дату возвращаемого объекта т.к новая дата создается внутри метода
        CommentDto expect = ItemMapper.commentToCommentDto(comment);

        // expect
        verify(commentStorage, times(1))
                .save(isA(Comment.class));
        verify(bookingStorage, times(1))
                .findFirstByItemId(anyLong());
        verifyNoMoreInteractions(bookingStorage, commentStorage);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testAddNewComment_ShouldReturnError_WhenBookingNotExists() {
        // given
        Long userId = 1L;
        Long itemId = 1L;

        when(bookingStorage.findFirstByItemId(anyLong()))
                .thenThrow(new BookingNotFoundException("Бронирование не найдено по ID вещи " + itemId));

        // expect
        final BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class,
                () -> itemService.addNewComment(userId, itemId, new CommentAddDto("Test")));

        assertThat(exception.getMessage(),
                equalTo("Бронирование не найдено по ID вещи " + itemId));
    }

    @Test
    void testAddNewComment_ShouldReturnError_WhenUserAreNotBooker() {
        // given
        Long itemId = 1L;
        Long userId = 2L;
        Booking booking = makeDefaultBooking();
        booking.setEnd(LocalDateTime.now().minusHours(1));

        when(bookingStorage.findFirstByItemId(anyLong()))
                .thenReturn(Optional.of(booking));
        // expect
        final CommentException exception = assertThrows(
                CommentException.class,
                () -> itemService.addNewComment(userId, itemId, new CommentAddDto("Test")));

        assertThat(exception.getMessage(),
                equalTo("Вы не можете оставить комментарий так как вы не бронировали эту вещь либо" +
                        " бронирование еще не закончено"));
    }

    @Test
    void testAddNewComment_ShouldReturnError_WhenBookingEnded() {
        // given
        Long itemId = 1L;
        Long userId = 1L;
        Booking booking = makeDefaultBooking();
        booking.setEnd(LocalDateTime.now().plusHours(1));

        when(bookingStorage.findFirstByItemId(anyLong()))
                .thenReturn(Optional.of(booking));
        // expect
        final CommentException exception = assertThrows(
                CommentException.class,
                () -> itemService.addNewComment(userId, itemId, new CommentAddDto("Test")));

        assertThat(exception.getMessage(),
                equalTo("Вы не можете оставить комментарий так как вы не бронировали эту вещь либо" +
                        " бронирование еще не закончено"));
    }
}