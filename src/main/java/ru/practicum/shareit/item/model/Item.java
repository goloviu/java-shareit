package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.request.ItemRequest;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @EqualsAndHashCode.Exclude
    private Long id;
    private Long owner;
    private String name;
    private String description;
    private Boolean available;
    private ItemRequest request;
}
