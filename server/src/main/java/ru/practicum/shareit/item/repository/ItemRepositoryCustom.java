package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

public interface ItemRepositoryCustom {

    Item saveItem(Item item, Long requestId);
}
