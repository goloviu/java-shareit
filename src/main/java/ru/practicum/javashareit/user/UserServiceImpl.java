package ru.practicum.javashareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.javashareit.user.dto.UserDto;
import ru.practicum.javashareit.user.dto.UserMapper;

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
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(final Long userId) {
        return UserMapper.toUserDto(repository.getUserById(userId));
    }

    @Override
    public UserDto saveUser(final User user) {
        return UserMapper.toUserDto(repository.save(user));
    }

    @Override
    public UserDto updateUser(final UserDto userDto, final Long userId) {
        userDto.setId(userId);
        return UserMapper.toUserDto(repository.updateUser(userDto));
    }

    @Override
    public UserDto deleteUser(final Long userId) {
        return UserMapper.toUserDto(repository.deleteUser(userId));
    }
}