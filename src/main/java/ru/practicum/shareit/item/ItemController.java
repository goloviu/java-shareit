package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        log.info("Получен GET запрос на получение предмета по ID {}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос на получение всех предметов владельца по ID {}", userId);
        return itemService.getOwnerItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findItemsByText(@RequestParam("text") String regEx) {
        log.info("Получен GET запрос на поиск предметов по ключевому слову {}", regEx);
        return itemService.findItemsByText(regEx);
    }

    @PostMapping
    public ItemDto addNewItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody @Valid Item item) {
        log.info("Получен POST запрос на добавление новой вещи от польхователя ID {}, вещи {}", userId, item);
        return itemService.addNewItem(userId, item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody @Valid ItemDto itemDto,
                              @PathVariable Long itemId) {
        log.info("Получен PATCH запрос на обновление вещи");
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Later-User-Id") Long userId,
                           @PathVariable Long itemId) {
        log.info("Получен DELETE запрос на удаление вещи из БД");
        itemService.deleteItem(userId, itemId);
    }
}
