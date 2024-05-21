package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

interface ItemRepository {

    List<Item> findItemsByUserId(final Long userId);

    Item getItemById(final Long itemId);

    List<Item> findItemsByText(final String regEx);

    Item save(final Item item);

    Item updateItem(final Item item);

    void deleteByUserIdAndItemId(final Long userId, final Long itemId);

    void checkItemExist(final Long itemId);
}
