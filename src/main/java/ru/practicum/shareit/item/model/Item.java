package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.request.ItemRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @EqualsAndHashCode.Exclude
    private Long id;
    private Long owner;
    @NotBlank(message = "Не указано название")
    @Size(max = 100, message = "Название предмета не может превышать 100 символов")
    private String name;
    @NotBlank(message = "Описание не указано")
    @Size(max = 500, message = "Описание предмета не может превышать 500 символов")
    private String description;
    @NotNull(message = "Возможность забронировать не может быть null")
    private Boolean available;
    private ItemRequest request;
}
