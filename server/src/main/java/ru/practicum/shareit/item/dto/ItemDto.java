package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private Long owner;
    @Size(max = 100, message = "Название предмета не может превышать 100 символов")
    private String name;
    @Size(max = 500, message = "Описание предмета не может превышать 500 символов")
    private String description;
    private Boolean available;
    private Long requestId;
}
