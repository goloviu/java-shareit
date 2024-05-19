package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;

public class ItemMapper {

    public static ItemDto toItemDto(final Item item) {
        @Valid
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .owner(item.getOwner())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(item.getRequest())
                .build();
        return itemDto;
    }

    public static Item toItem(final ItemDto itemDto) {
        @Valid
        Item item = Item.builder()
                .id(itemDto.getId())
                .owner(itemDto.getOwner())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .request(itemDto.getRequest())
                .build();
        return item;
    }
}
