package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.model.User;

import javax.validation.Valid;

public class UserMapper {

    public static UserDto toUserDto(final User user) {
        @Valid
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
        return userDto;
    }
}
