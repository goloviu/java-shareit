package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    @Email(message = "Неверный формат почты")
    private String email;
    @Size(max = 40, message = "Имя/логин пользователя не должен превышать 40 символов")
    private String name;
}
