package ru.practicum.shareit.item;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {

    List<ItemWithBookingDto> getOwnerItemsWithBookings(final Long userId, final PageRequest page);

    ItemWithBookingDto getItemByIdWithBooking(final Long userId, final Long itemId);

    List<ItemDto> findItemsByText(final String regEx, final Pageable page);

    ItemDto addNewItem(final Long userId, final ItemRegisterDto item);

    ItemDto updateItem(final Long userId, final Long itemId, final ItemDto itemDto);

    void deleteItem(final Long userId, final Long itemId);

    CommentDto addNewComment(final Long userId, final Long itemId, final CommentAddDto commentAddDto);
}
