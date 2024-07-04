package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRegisterDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Получен GET запрос на получение всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("Получен GET запрос на получение пользователя по ID {}", userId);
        return userService.getUserById(userId);
    }

    @PostMapping
    public UserDto saveNewUser(@RequestBody @Valid UserRegisterDto userRegisterDto) {
        log.info("Получен POST запрос на сохранение пользователя {}", userRegisterDto);
        return userService.saveUser(userRegisterDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestBody @Valid UserDto userDto,
                              @PathVariable Long userId) {
        log.info("Получен PATCH запрос на обновление данных пользователя ID {}. Новые данные: \n {}", userId, userDto);
        return userService.updateUser(userDto, userId);
    }

    @DeleteMapping("/{userId}")
    public boolean deleteUser(@PathVariable Long userId) {
        log.info("Получен DELETE запрос на удаление пользователя по ID {}", userId);
        return userService.deleteUser(userId);
    }
}
