package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

public class ItemMapper {

    public static ItemDto itemToItemDto(final Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .owner(item.getOwner())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(item.getRequest())
                .build();
    }

    public static Item itemDtoToItem(final ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .owner(itemDto.getOwner())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .request(itemDto.getRequest())
                .build();
    }

    public static Item itemRegisterDtoToItem(final ItemRegisterDto itemRegisterDto) {
        return Item.builder()
                .owner(itemRegisterDto.getOwner())
                .name(itemRegisterDto.getName())
                .description(itemRegisterDto.getDescription())
                .available(itemRegisterDto.getAvailable())
                .request(itemRegisterDto.getRequest())
                .build();
    }
}
