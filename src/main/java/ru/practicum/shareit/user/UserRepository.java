package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {

    List<User> findAll();

    User getUserById(final Long userId);

    User save(final User user);

    User updateUser(final User user);

    User deleteUser(final Long userId);

    void checkUserExist(final Long userId);
}
