package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest newItemRequestDtoToItemRequest(final NewItemRequestDto requestDto, final Long userId) {
        return ItemRequest.builder()
                .description(requestDto.getDescription())
                .created(LocalDateTime.now())
                .requestorId(userId)
                .build();
    }

    public static ItemRequestDto itemRequestToItemRequestDto(final ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequestWithAnswerDto itemRequestToItemRequestWithAnswerDto(final ItemRequest itemRequest,
                                                                                 List<Item> items) {
        List<ItemForRequestDto> itemDtos = items.stream()
                .map(ItemMapper::itemToItemForRequestDto)
                .collect(Collectors.toList());

        return ItemRequestWithAnswerDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(itemDtos)
                .build();
    }
}
