package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRegisterDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(final Long userId);

    UserDto saveUser(final UserRegisterDto userRegisterDto);

    UserDto updateUser(final UserDto userDto, final Long userId);

    boolean deleteUser(final Long userId);
}
