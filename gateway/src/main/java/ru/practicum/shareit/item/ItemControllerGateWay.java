package ru.practicum.shareit.item;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemForUpdateDto;
import ru.practicum.shareit.item.dto.ItemRegisterDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemControllerGateWay {
    private final ItemClient itemClient;

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long itemId) {
        log.info("GateWay GET запрос на получение предмета по ID {}, от пользователя ID {}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(defaultValue = "1") @Positive Integer from,
                                                @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("GateWay GET запрос на получение всех предметов владельца по ID {}", userId);
        return itemClient.getOwnerItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findItemsByText(@RequestParam("text") String regEx,
                                                  @RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(defaultValue = "1") @Positive Integer from,
                                                  @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("GateWay GET запрос на поиск предметов по ключевому слову {}", regEx);
        return itemClient.findItemsByText(userId, regEx, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> addNewItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestBody @Valid ItemRegisterDto itemRegisterDto) {
        log.info("Получен POST запрос на добавление новой вещи от пользователя ID {}, вещи {}", userId, itemRegisterDto);
        return itemClient.saveNewItem(userId, itemRegisterDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody @Valid ItemForUpdateDto itemDto) {
        log.info("GateWay PATCH запрос на обновление вещи ID {}, пользователя ID {}, информация обновления: \n {}",
                itemId, userId, itemDto);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId) {
        log.info("GateWay DELETE запрос на удаление вещи из БД");
        return itemClient.deleteItem(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addNewComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @PathVariable Long itemId,
                                                @RequestBody @Valid CommentDto comment) {
        return itemClient.addNewComment(userId, itemId, comment);
    }
}
