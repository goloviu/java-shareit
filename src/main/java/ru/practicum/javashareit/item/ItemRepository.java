package ru.practicum.javashareit.item;

import ru.practicum.javashareit.item.dto.ItemDto;

import java.util.List;

interface ItemRepository {

    List<Item> findItemsByUserId(final Long userId);

    Item getItemById(final Long itemId);

    List<Item> findItemsByText(final String regEx);

    Item save(final Item item);

    Item updateItem(final ItemDto itemDto);

    void deleteByUserIdAndItemId(final Long userId, final Long itemId);

    void checkItemExist(final Long itemId);
}