package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserRegisterDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        log.info("Сервис обработал запрос на получение пользователей. \n {}", users);
        return users.stream()
                .map(UserMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(final Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден по ID " + userId));

        log.info("Сервис обработал запрос на нахождение пользователя по ID {},\n {}", userId, user);
        return UserMapper.userToUserDto(user);
    }

    @Override
    public UserDto saveUser(final UserRegisterDto userRegisterDto) {
        User registredUser = UserMapper.userRegisterDtoToUser(userRegisterDto);
        User savedUser = userRepository.save(registredUser);
        log.info("Сервис обработал запрос на сохранение пользователя в БД по запросу {}, Результат: \n {}",
                registredUser, savedUser);
        return UserMapper.userToUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(final UserDto userDto, final Long userId) {
        if (!isUserExists(userId) || userId == null) {
            throw new IllegalArgumentException("Некорректно указан ID пользователя: " + userId);
        }

        userDto.setId(userId);
        checkUserEmail(userDto);
        User updatedUser = userRepository.save(updateFields(userDto));
        log.info("Сервис обработал запрос на обновление пользователя в по запросу {}, Результат: \n {}",
                userDto, updatedUser);
        return UserMapper.userToUserDto(updatedUser);
    }

    @Override
    @Transactional
    public boolean deleteUser(final Long userId) {
        if (!isUserExists(userId)) {
            throw new UserNotFoundException("Пользователь не найден по ID " + userId);
        }

        log.info("Сервис обработал запрос на удаление пользователя из БД по ID {}", userId);
        return userRepository.removeById(userId) > 0;
    }

    private boolean isUserExists(final Long userId) {
        return userRepository.existsById(userId);
    }

    private void checkUserEmail(final UserDto userDto) {
        if (userDto.getEmail() == null) {
            return;
        }

        ExampleMatcher caseInsensitiveExampleMatcher = ExampleMatcher.matchingAll().withIgnoreCase();
        Example<User> userEmailExample = Example.of(new User(userDto.getEmail()), caseInsensitiveExampleMatcher);
        Example<User> userSameIdExample = Example.of(new User(userDto.getId(), userDto.getEmail(), null),
                caseInsensitiveExampleMatcher);

        if (userRepository.exists(userEmailExample) && !userRepository.exists(userSameIdExample)) {
            throw new IllegalArgumentException("Данная почта уже существует " + userDto);
        }
    }

    private User updateFields(final UserDto userDto) {
        User dbUser = userRepository.findById(userDto.getId()).get();

        if (userDto.getName() != null && userDto.getEmail() != null) {
            dbUser.setName(userDto.getName());
            dbUser.setEmail(userDto.getEmail());
        } else if (userDto.getName() != null) {
            dbUser.setName(userDto.getName());
        } else if (userDto.getEmail() != null) {
            dbUser.setEmail(userDto.getEmail());
        }
        return dbUser;
    }
}
