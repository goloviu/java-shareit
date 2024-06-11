package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {

    List<ItemWithBookingDto> getOwnerItemsWithBookings(final Long userId);

    ItemWithBookingDto getItemByIdWithBooking(final Long userId, final Long itemId);

    List<ItemDto> findItemsByText(final String regEx);

    ItemDto addNewItem(final Long userId, final ItemRegisterDto item);

    ItemDto updateItem(final Long userId, final Long itemId, final ItemDto itemDto);

    void deleteItem(final Long userId, final Long itemId);

    CommentDto addNewComment(final Long userId, final Long itemId, final CommentAddDto commentAddDto);
}
