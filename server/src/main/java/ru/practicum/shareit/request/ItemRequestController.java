package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswerDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private ItemRequestService requestService;

    @Autowired
    public ItemRequestController(ItemRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ItemRequestDto makeRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @RequestBody NewItemRequestDto request) {
        log.info("Получен POST запрос на создание нового запроса на вещь. Пользователь ID {}, запрос: {}",
                userId, request);
        return requestService.addNewRequest(userId, request);
    }

    @GetMapping
    public List<ItemRequestWithAnswerDto> getOwnUserItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос на получение запросов на вещи с ответами. Пользователь ID {}", userId);
        return requestService.getOwnUserItemRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithAnswerDto> getUsersRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                           @RequestParam(defaultValue = "0") Integer from,
                                                           @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен GET запрос на получение всех запросов пользователей на вещи. Пользователь ID {}," +
                " Страница: {} Размер: {}", userId, from, size);
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by("created").ascending());
        return requestService.getUsersItemRequests(userId, pageRequest);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithAnswerDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @PathVariable Long requestId) {
        log.info("Получен GET запрос на получение запроса на вещи с ответами. Пользователь ID {}, запрос ID {}",
                userId, requestId);
        return requestService.getRequestById(userId, requestId);
    }
}
