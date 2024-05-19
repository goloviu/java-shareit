package ru.practicum.javashareit.user;

import ru.practicum.javashareit.user.dto.UserDto;

import java.util.List;

interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(final Long userId);

    UserDto saveUser(final User user);

    UserDto updateUser(final UserDto userDto, final Long userId);

    UserDto deleteUser(final Long userId);
}