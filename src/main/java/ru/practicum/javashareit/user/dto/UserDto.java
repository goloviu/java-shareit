package ru.practicum.javashareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    @Email(message = "Неверный формат почты")
    private String email;
    @Size(max = 40, message = "Имя/логин пользователя не должен превышать 40 символов")
    private String name;
}