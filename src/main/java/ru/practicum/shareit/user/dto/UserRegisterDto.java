package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
public class UserRegisterDto {
    @NotBlank(message = "Поле email не указано")
    @Email(message = "Неверный формат почты")
    private String email;
    @NotBlank(message = "Имя или логин не указан")
    @Size(max = 40, message = "Имя/логин пользователя не должен превышать 40 символов")
    private String name;
}
