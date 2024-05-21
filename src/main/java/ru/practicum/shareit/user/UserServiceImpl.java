package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserRegisterDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = repository.findAll();
        return users.stream()
                .map(UserMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(final Long userId) {
        return UserMapper.userToUserDto(repository.getUserById(userId));
    }

    @Override
    public UserDto saveUser(final UserRegisterDto userRegisterDto) {
        return UserMapper.userToUserDto(repository.save(UserMapper.userRegisterDtoToUser(userRegisterDto)));
    }

    @Override
    public UserDto updateUser(final UserDto userDto, final Long userId) {
        userDto.setId(userId);
        return UserMapper.userToUserDto(repository.updateUser(UserMapper.userDtoToUser(userDto)));
    }

    @Override
    public UserDto deleteUser(final Long userId) {
        return UserMapper.userToUserDto(repository.deleteUser(userId));
    }
}
