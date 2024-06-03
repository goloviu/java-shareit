package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserRegisterDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Autowired
    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = repository.findAll();
        return users.stream()
                .map(UserMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(final Long userId) {
        return UserMapper.userToUserDto(repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден по ID " + userId)));
    }

    @Override
    public UserDto saveUser(final UserRegisterDto userRegisterDto) {
        return UserMapper.userToUserDto(repository.save(UserMapper.userRegisterDtoToUser(userRegisterDto)));
    }

    @Override
    public UserDto updateUser(final UserDto userDto, final Long userId) {
        userDto.setId(userId);
        return UserMapper.userToUserDto(repository.save(UserMapper.userDtoToUser(userDto)));
    }

    @Override
    public UserDto deleteUser(final Long userId) {
        return UserMapper.userToUserDto(repository.removeById(userId));
    }
}
