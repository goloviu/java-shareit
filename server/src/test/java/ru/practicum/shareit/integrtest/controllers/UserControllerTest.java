package ru.practicum.shareit.integrtest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserRegisterDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    private static final String URL = "http://localhost:8080/users";

    @Test
    void testGetAllUsers_ShouldReturnListUserDto_WhenUserExists() throws Exception {
        // given
        User user = User.builder()
                .id(1L)
                .name("Test name")
                .email("teseemail@email.ru")
                .build();
        UserDto userDto = UserMapper.userToUserDto(user);

        when(userService.getAllUsers())
                .thenReturn(List.of(userDto));

        // expect
        mvc.perform(get(URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.[0].email", is(user.getEmail())))
                .andExpect(jsonPath("$.[0].name", is(user.getName())));
    }

    @Test
    void testGetUserById_ShouldReturnUserDtoById_WhenUserExists() throws Exception {
        // given
        User user = User.builder()
                .id(1L)
                .name("Test name")
                .email("teseemail@email.ru")
                .build();
        UserDto userDto = UserMapper.userToUserDto(user);

        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);

        // expect
        mvc.perform(get(URL.concat("/{userId}"), user.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.name", is(user.getName())));
    }

    @Test
    void testGetUserById_ShouldReturnError_WhenUserNotExists() throws Exception {
        // given
        when(userService.getUserById(anyLong()))
                .thenThrow(new UserNotFoundException("Пользователь не найден"));

        // expect
        mvc.perform(get(URL.concat("/{userId}"), 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSaveNewUser_ShouldReturnSavedUser_WhenUserNotNullAndNotExists() throws Exception {
        // given
        User user = User.builder()
                .id(1L)
                .name("Test name")
                .email("teseemail@email.ru")
                .build();
        UserDto userDto = UserMapper.userToUserDto(user);
        UserRegisterDto userRegisterDto = UserRegisterDto.builder()
                .name("Test name")
                .email("teseemail@email.ru")
                .build();

        when(userService.saveUser(userRegisterDto))
                .thenReturn(userDto);

        // expect
        mvc.perform(post(URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(mapper.writeValueAsString(userRegisterDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.name", is(user.getName())));
    }

    @Test
    void testUpdateUser_ShouldReturnUpdatedUser_WhenUserExists() throws Exception {
        // given
        User user = User.builder()
                .id(1L)
                .name("Test name")
                .email("teseemail@email.ru")
                .build();
        UserDto userDto = UserMapper.userToUserDto(user);

        when(userService.updateUser(userDto, user.getId()))
                .thenReturn(userDto);

        // expect
        mvc.perform(patch(URL.concat("/{userId}"), user.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.name", is(user.getName())));
    }

    @Test
    void testDeleteUser_ShouldRemoveUser_WhenUserExists() throws Exception {
        // given
        User user = User.builder()
                .id(1L)
                .name("Test name")
                .email("teseemail@email.ru")
                .build();
        UserDto userDto = UserMapper.userToUserDto(user);

        when(userService.deleteUser(user.getId()))
                .thenReturn(true);

        // expect
        mvc.perform(delete(URL.concat("/{userId}"), user.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("true"));
    }
}