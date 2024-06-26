package ru.practicum.shareit.integrtest.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
class UserRepositoryTest {

    private UserRepository userRepository;
    private User user;

    @Autowired
    public UserRepositoryTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .name("Test name")
                .email("teseemail@email.ru")
                .build();

        userRepository.save(user);
    }

    @AfterEach
    public void tearDown() {
        // Release test data after each test method
        userRepository.delete(user);
    }

    @Test
    void testRemoveById_ShouldReturnCountedDeletedElements_WhenUserExists() {
        // given
        Optional<User> optionalUser = userRepository.findById(user.getId());
        assertThat(optionalUser.isPresent(), equalTo(true));

        // do
        Integer deletedElements = userRepository.removeById(user.getId());
        Optional<User> result = userRepository.findById(user.getId());
        Boolean expect = false;

        // expect
        assertThat(deletedElements, equalTo(1));
        assertThat(result.isPresent(), equalTo(expect));
    }
}