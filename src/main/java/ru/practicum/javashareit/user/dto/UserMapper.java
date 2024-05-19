package ru.practicum.javashareit.user.dto;


import ru.practicum.javashareit.user.User;

public class UserMapper {

    public static UserDto toUserDto(final User user) {
        return new UserDto().builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
