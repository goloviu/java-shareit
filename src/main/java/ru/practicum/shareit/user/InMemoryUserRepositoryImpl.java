package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.UserAlreadyExistsException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class InMemoryUserRepositoryImpl implements UserRepository {
    private final HashMap<Long, User> users = new HashMap<>();
    private Long id = 1L;

    @Override
    public List<User> findAll() {
        log.info("Получен список всех пользователей из БД");
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long userId) {
        checkUserExist(userId);

        return users.get(userId);
    }

    @Override
    public User save(User user) {
        if (users.containsValue(user)) {
            throw new UserAlreadyExistsException("Пользователь уже существует");
        }  else if (user.getId() != null) {
            throw new IllegalArgumentException("У нового пользователя не должен быть указан ID");
        }
        user.setId(generateId());
        users.put(user.getId(), user);
        log.info("Пользователь с ID {} успешно сохранен в БД. \n {}", user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(UserDto userDto) {
        if (userDto.getId() == null || !users.containsKey(userDto.getId())) {
            throw new IllegalArgumentException("Некорректно указан ID пользователя: " + userDto.getId());
        }

        checkUserExist(userDto.getId());
        checkEmailExist(userDto);

        User userForUpdate = users.get(userDto.getId());
        User userBeforeUpdate = User.builder().
                id(userForUpdate.getId())
                .name(userForUpdate.getName())
                .email(userForUpdate.getEmail())
                .build();

        User userAfterUpdate = updateFields(userDto);
        users.put(userDto.getId(), userAfterUpdate);
        log.info("Пользователь с ID {} успешно обновлен в БД. \nБыло: {} \nСтало: {}", userDto.getId(), userBeforeUpdate,
                userAfterUpdate);
        return userAfterUpdate;
    }

    @Override
    public User deleteUser(Long userId) {
        checkUserExist(userId);

        User deletedUser = users.remove(userId);
        log.info("Пользователь по ID {} успешно удален из БД. Дополнительная информация: {}", userId, deletedUser);
        return deletedUser;
    }

    @Override
    public void checkUserExist(Long userId) {
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь не найден по ID " + userId);
        }
    }

    private User updateFields(final UserDto userDto) {
        User user = users.get(userDto.getId());

        if (userDto.getName() != null && userDto.getEmail() != null) {
            user.setName(userDto.getName());
            user.setEmail(userDto.getEmail());
        } else if (userDto.getName() != null) {
            user.setName(userDto.getName());
        } else if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return user;
    }

    private void checkEmailExist(final UserDto userDto) {
        Boolean isEmailExist = users.values().stream()
                .anyMatch(user -> user.getEmail().equals(userDto.getEmail()) && !user.getId().equals(userDto.getId()));
        if (isEmailExist) {
            throw new IllegalArgumentException("Данная почта уже существует в БД " + userDto.getEmail());
        }
    }

    private Long generateId() {
        return id++;
    }
}