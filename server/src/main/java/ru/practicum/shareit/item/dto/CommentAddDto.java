package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentAddDto {

    @Size(max = 512, message = "Комментарий не может превышать 512 симоволов")
    @NotBlank
    private String text;
}
