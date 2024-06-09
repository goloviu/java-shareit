package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CommentAddDto {

    @Size(max = 512, message = "Комментарий не может превышать 512 симоволов")
    @NotBlank
    private String text;
}
