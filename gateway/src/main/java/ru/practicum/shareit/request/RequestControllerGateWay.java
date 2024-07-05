package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestControllerGateWay {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> makeRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestBody @Valid NewItemRequestDto request) {
        log.info("GateWay POST запрос на создание нового запроса на вещь. Пользователь ID {}, запрос: {}",
                userId, request);
        return requestClient.addNewRequest(userId, request);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnUserItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GateWay GET запрос на получение запросов на вещи с ответами. Пользователь ID {}", userId);
        return requestClient.getOwnUserItemRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getUsersRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                   @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("GateWay GET запрос на получение всех запросов пользователей на вещи. Пользователь ID {}," +
                " Страница: {} Размер: {}", userId, from, size);
        return requestClient.getUsersItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long requestId) {
        log.info("GateWay GET запрос на получение запроса на вещи с ответами. Пользователь ID {}, запрос ID {}",
                userId, requestId);
        return requestClient.getRequestById(userId, requestId);
    }
}
