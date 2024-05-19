package ru.practicum.javashareit.item.dto;

import ru.practicum.javashareit.item.Item;

import javax.validation.Valid;

public class ItemMapper {

    public static ItemDto toItemDto(final Item item) {
        @Valid
        ItemDto itemDto = new ItemDto().builder()
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
        Item item = new Item().builder()
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
