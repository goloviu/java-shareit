package ru.practicum.javashareit.item;

import ru.practicum.javashareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    List<ItemDto> getOwnerItems(final Long userId);

    ItemDto getItemById(final Long itemId);

    List<ItemDto> findItemsByText(final String regEx);

    ItemDto addNewItem(final Long userId, final Item item);

    ItemDto updateItem(final Long userId, final Long itemId, final ItemDto itemDto);

    void deleteItem(final Long userId, final Long itemId);
}
