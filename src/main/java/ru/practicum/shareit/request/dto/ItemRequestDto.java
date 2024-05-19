package ru.practicum.shareit.request.dto;

import javax.validation.constraints.NotBlank;

public class ItemRequestDto {
    private Integer id;
    @NotBlank(message = "Не указан ID пользователя, создавшего запрос")
    private Integer requestorId;
    @NotBlank(message = "Описание не задано")
    private String description;
    private String created;
}
