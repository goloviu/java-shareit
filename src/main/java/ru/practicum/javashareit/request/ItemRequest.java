package ru.practicum.javashareit.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ItemRequest {
    private Integer id;
    @NotBlank(message = "Не указан ID пользователя, создавшего запрос")
    private Integer requestorId;
    @NotBlank(message = "Описание не задано")
    private String description;
    private String created;
}
