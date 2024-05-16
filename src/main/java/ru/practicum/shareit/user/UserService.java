package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(final Long userId);

    UserDto saveUser(final User user);

    UserDto updateUser(final UserDto userDto, final Long userId);

    UserDto deleteUser(final Long userId);
}
