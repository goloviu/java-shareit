package ru.practicum.shareit.test.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserRegisterDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    private void setUp() {
        this.userService = new UserServiceImpl(userRepository);
    }

    private User makeDefaultUser() {
        return User.builder()
                .id(1L)
                .email("testemail@test.test")
                .name("USER TEST NAME")
                .build();
    }

    @Test
    void testGetAllUsers_ShouldReturnListUserDto_WhenUserExists() {
        // given
        User user = makeDefaultUser();

        when(userRepository.findAll())
                .thenReturn(List.of(user));

        // do
        List<UserDto> result = userService.getAllUsers();
        List<UserDto> expect = List.of(UserMapper.userToUserDto(user));

        // expect
        verify(userRepository, times(1))
                .findAll();
        verifyNoMoreInteractions(userRepository);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testGetUserById_ShouldReturnUserById_WhenUserExists() {
        // given
        User user = makeDefaultUser();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        // do
        UserDto result = userService.getUserById(user.getId());
        UserDto expect = UserMapper.userToUserDto(user);

        // expect
        verify(userRepository, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(userRepository);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testGetUserById_ShouldReturnError_WhenUserNotExists() {
        // given
        User user = makeDefaultUser();
        when(userRepository.findById(anyLong()))
                .thenThrow(new UserNotFoundException("Пользователь не найден по ID " + user.getId()));

        // expect
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(user.getId()));

        assertThat(exception.getMessage(),
                equalTo("Пользователь не найден по ID " + user.getId()));
    }

    @Test
    void testSaveUser_ShouldSaveUser_WhenUserNotExists() {
        // given
        User user = makeDefaultUser();
        UserRegisterDto userRegisterDto = UserRegisterDto.builder()
                .name("USER TEST NAME")
                .email("testemail@test.test")
                .build();

        when(userRepository.save(isA(User.class)))
                .thenReturn(user);
        // do
        UserDto result = userService.saveUser(userRegisterDto);
        UserDto expect = UserMapper.userToUserDto(user);

        // expect
        verify(userRepository, times(1))
                .save(isA(User.class));
        verifyNoMoreInteractions(userRepository);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testUpdateUser_ShouldReturnUpdatedUser_WhenUserExists() {
        // given
        User user = makeDefaultUser();
        UserDto userDto = UserMapper.userToUserDto(user);

        when(userRepository.save(isA(User.class)))
                .thenReturn(user);
        when(userRepository.exists(isA(Example.class)))
                .thenReturn(true);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        // do
        UserDto result = userService.updateUser(userDto, userDto.getId());
        UserDto expect = UserMapper.userToUserDto(user);

        // expect
        verify(userRepository, times(1))
                .save(isA(User.class));
        verify(userRepository, times(2))
                .exists(isA(Example.class));
        verify(userRepository, times(1))
                .existsById(anyLong());
        verify(userRepository, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(userRepository);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testUpdateUser_ShouldReturnUpdatedUserName_WhenUserExists() {
        // given
        User user = makeDefaultUser();
        UserDto userDto = UserMapper.userToUserDto(user);
        userDto.setEmail(null);

        when(userRepository.save(isA(User.class)))
                .thenReturn(user);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        // do
        UserDto result = userService.updateUser(userDto, userDto.getId());
        UserDto expect = UserMapper.userToUserDto(user);

        // expect
        verify(userRepository, times(1))
                .save(isA(User.class));
        verify(userRepository, times(1))
                .existsById(anyLong());
        verify(userRepository, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(userRepository);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testUpdateUser_ShouldReturnUpdatedUserEmail_WhenUserExists() {
        // given
        User user = makeDefaultUser();
        UserDto userDto = UserMapper.userToUserDto(user);
        userDto.setName(null);

        when(userRepository.save(isA(User.class)))
                .thenReturn(user);
        when(userRepository.exists(isA(Example.class)))
                .thenReturn(true);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        // do
        UserDto result = userService.updateUser(userDto, userDto.getId());
        UserDto expect = UserMapper.userToUserDto(user);

        // expect
        verify(userRepository, times(1))
                .save(isA(User.class));
        verify(userRepository, times(2))
                .exists(isA(Example.class));
        verify(userRepository, times(1))
                .existsById(anyLong());
        verify(userRepository, times(1))
                .findById(anyLong());
        verifyNoMoreInteractions(userRepository);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testUpdateUser_ShouldReturnError_WhenUserNotExists() {
        // given
        User user = makeDefaultUser();
        UserDto userDto = UserMapper.userToUserDto(user);

        when(userRepository.existsById(anyLong()))
                .thenReturn(false);

        // expect
        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(userDto, userDto.getId()));

        assertThat(exception.getMessage(),
                equalTo("Некорректно указан ID пользователя: " + userDto.getId()));
    }

    @Test
    void testUpdateUser_ShouldReturnError_WhenUserIdIsNull() {
        // given
        User user = makeDefaultUser();
        UserDto userDto = UserMapper.userToUserDto(user);

        when(userRepository.existsById(nullable(Long.class)))
                .thenReturn(true);

        // expect
        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(userDto, null));

        assertThat(exception.getMessage(),
                equalTo("Некорректно указан ID пользователя: " + null));
    }

    @Test
    void testUpdateUser_ShouldReturnError_WhenUserNotUnique() {
        // given
        User user = makeDefaultUser();
        UserDto userDto = UserMapper.userToUserDto(user);

        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.exists(isA(Example.class)))
                .thenThrow(new IllegalArgumentException("Данная почта уже существует " + userDto));

        // expect
        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(userDto, userDto.getId()));

        assertThat(exception.getMessage(),
                equalTo("Данная почта уже существует " + userDto));
    }

    @Test
    void testDeleteUser_ShouldReturnTrue_WhenUserSuccesfullyDeleted() {
        // given
        Long userId = 1L;

        when(userRepository.removeById(anyLong()))
                .thenReturn(1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        // do
        Boolean result = userService.deleteUser(userId);
        Boolean expect = true;

        // expect
        verify(userRepository, times(1))
                .removeById(anyLong());
        verify(userRepository, times(1))
                .existsById(anyLong());
        verifyNoMoreInteractions(userRepository);
        assertThat(result, equalTo(expect));
    }

    @Test
    void testDeleteUser_ShouldReturnError_WhenUserNotExists() {
        // given
        Long userId = 1L;

        when(userRepository.existsById(anyLong()))
                .thenReturn(false);

        // expect
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(userId));

        assertThat(exception.getMessage(),
                equalTo("Пользователь не найден по ID " + userId));
    }
}