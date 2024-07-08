package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRegisterDto {
    private Long owner;
    @NotBlank(message = "Не указано название")
    @Size(max = 100, message = "Название предмета не может превышать 100 символов")
    private String name;
    @NotBlank(message = "Описание не указано")
    @Size(max = 500, message = "Описание предмета не может превышать 500 символов")
    private String description;
    @NotNull(message = "Возможность забронировать не может быть null")
    private Boolean available;
    private Long requestId;
}
