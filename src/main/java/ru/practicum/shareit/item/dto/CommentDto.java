package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {

    private Long id;
    @Size(max = 512, message = "Комментарий не может превышать 512 симоволов")
    private String text;
    @NotBlank
    private String authorName;
    @NotBlank
    private LocalDateTime created;
}
