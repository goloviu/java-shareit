package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.UserAlreadyExistsException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
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
        } else if (user.getId() != null) {
            throw new IllegalArgumentException("У нового пользователя не должен быть указан ID");
        }

        user.setId(generateId());
        checkEmailExist(user);
        users.put(user.getId(), user);
        log.info("Пользователь с ID {} успешно сохранен в БД. \n {}", user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new IllegalArgumentException("Некорректно указан ID пользователя: " + user.getId());
        }

        checkUserExist(user.getId());
        checkEmailExist(user);

        User userForUpdate = users.get(user.getId());
        User userBeforeUpdate = User.builder()
                .id(userForUpdate.getId())
                .name(userForUpdate.getName())
                .email(userForUpdate.getEmail())
                .build();

        User userAfterUpdate = updateFields(user);
        users.put(user.getId(), userAfterUpdate);
        log.info("Пользователь с ID {} успешно обновлен в БД. \nБыло: {} \nСтало: {}", user.getId(), userBeforeUpdate,
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

    private User updateFields(final User user) {
        User dbUser = users.get(user.getId());

        if (user.getName() != null && user.getEmail() != null) {
            dbUser.setName(user.getName());
            dbUser.setEmail(user.getEmail());
        } else if (user.getName() != null) {
            dbUser.setName(user.getName());
        } else if (user.getEmail() != null) {
            dbUser.setEmail(user.getEmail());
        }
        return dbUser;
    }

    private void checkEmailExist(final User user) {
        Boolean isEmailExist = users.values().stream()
                .anyMatch(dbUser -> dbUser.getEmail().equals(user.getEmail()) && !dbUser.getId().equals(user.getId()));
        if (isEmailExist) {
            throw new IllegalArgumentException("Данная почта уже существует в БД " + user.getEmail());
        }
    }

    private Long generateId() {
        return id++;
    }
}