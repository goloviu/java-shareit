package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRegisterDto;

import java.util.List;

public interface ItemService {

    List<ItemDto> getOwnerItems(final Long userId);

    ItemDto getItemById(final Long itemId);

    List<ItemDto> findItemsByText(final String regEx);

    ItemDto addNewItem(final Long userId, final ItemRegisterDto item);

    ItemDto updateItem(final Long userId, final Long itemId, final ItemDto itemDto);

    void deleteItem(final Long userId, final Long itemId);
}
