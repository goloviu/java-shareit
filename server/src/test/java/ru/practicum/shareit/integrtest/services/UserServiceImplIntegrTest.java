package ru.practicum.shareit.integrtest.services;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Transactional
@SpringBootTest(properties = "db.name=test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplIntegrTest {

    private final UserService userService;
    private final UserRepository userRepository;

    private User user;
    private User user2;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .name("Test name")
                .email("teseemail@email.ru")
                .build();

        user2 = User.builder()
                .name("Test name2")
                .email("teseemail2@email.ru")
                .build();

        userRepository.save(user);
        userRepository.save(user2);
    }

    @AfterEach
    public void tearDown() {
        // Release test data after each test method
        userRepository.deleteById(user.getId());
        userRepository.deleteById(user2.getId());
    }

    @Test
    void testGetAllUsers_ShouldReturnListUserDto_WhenUsersExists() {
        // do
        List<UserDto> result = userService.getAllUsers();

        // expect
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getName(), equalTo(user.getName()));
        assertThat(result.get(0).getEmail(), equalTo(user.getEmail()));
        assertThat(result.get(1).getName(), equalTo(user2.getName()));
        assertThat(result.get(1).getEmail(), equalTo(user2.getEmail()));
    }
}