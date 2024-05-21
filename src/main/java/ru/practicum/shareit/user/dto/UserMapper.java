package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.model.User;

public class UserMapper {

    public static UserDto userToUserDto(final User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static User userRegisterDtoToUser(final UserRegisterDto userRegisterDto) {
        return User.builder()
                .email(userRegisterDto.getEmail())
                .name(userRegisterDto.getName())
                .build();
    }

    public static User userDtoToUser(final UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}
