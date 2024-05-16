package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ImMemoryItemRepositoryImpl implements ItemRepository {
    private Long itemId = 1L;

    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public List<Item> findItemsByUserId(final Long userId) {
        log.info("Получен список предметов пользователя ID {} из БД\n {}", userId, items.values());
        return items.values().stream()
                .filter(item -> item.getOwner().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Item getItemById(final Long itemId) {
        checkItemExist(itemId);

        return items.get(itemId);
    }

    @Override
    public List<Item> findItemsByText(String regEx) {
        if (regEx == null || regEx.isBlank()) {
            return Collections.EMPTY_LIST;
        }

        List<Item> foundItems = items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(regEx.toLowerCase())
                        || item.getDescription().toLowerCase().contains(regEx.toLowerCase()))
                .filter(item -> item.getAvailable().equals(true))
                .collect(Collectors.toList());
        log.info("Получен список предметов по ключевому тексту {} \n {}", regEx, foundItems);
        return foundItems;
    }

    @Override
    public Item save(final Item item) {
        if (items.containsValue(item)) {
            throw new IllegalArgumentException("Вещь уже существует");
        } else if (item.getId() != null) {
            throw new IllegalArgumentException("У новой вещи не должен быть указан ID");
        }

        item.setId(generateItemId());
        items.put(item.getId(), item);
        log.info("Добавлена новая вещь в БД: \n {}", item);
        return item;
    }

    @Override
    public Item updateItem(final ItemDto itemDto) {
        if (itemDto.getId() == null || !items.containsKey(itemDto.getId())) {
            throw new IllegalArgumentException("Некорректно указан ID предмета: " + itemDto.getId());
        }

        checkItemExist(itemDto.getId());

        Item itemForUpdate = items.get(itemDto.getId());

        Item itemBeforeUpdate = Item.builder()
                .id(itemForUpdate.getId())
                .owner(itemForUpdate.getOwner())
                .name(itemForUpdate.getName())
                .description(itemForUpdate.getDescription())
                .available(itemForUpdate.getAvailable())
                .request(itemForUpdate.getRequest())
                .build();

        updateFields(itemDto);

        items.put(itemDto.getId(), itemForUpdate);
        log.info("Предмет с ID {} успешно обновлен в БД. \nБыло: {} \nСтало: {}", itemDto.getId(), itemBeforeUpdate,
                itemDto);
        return itemForUpdate;
    }

    @Override
    public void deleteByUserIdAndItemId(final Long userId, final Long itemId) {
        if (items.containsKey(itemId)) {
            if (items.get(itemId).getOwner().equals(userId)) {
                items.remove(itemId);
            }
        }
    }

    @Override
    public void checkItemExist(Long itemId) {
        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Предмет не найден по ID " + itemId);
        }
    }

    private Long generateItemId() {
        return itemId++;
    }

    private Item updateFields(final ItemDto itemDto) {
        Item item = items.get(itemDto.getId());

        if (itemDto.getName() != null && itemDto.getDescription() != null && itemDto.getAvailable() != null) {
            item.setName(itemDto.getName());
            item.setDescription(itemDto.getDescription());
            item.setAvailable(itemDto.getAvailable());
        } else if (itemDto.getName() != null && itemDto.getDescription() != null) {
            item.setName(itemDto.getName());
            item.setDescription(itemDto.getDescription());
        } else if (itemDto.getName() != null && itemDto.getAvailable() != null) {
            item.setName(itemDto.getName());
            item.setAvailable(itemDto.getAvailable());
        } else if (itemDto.getDescription() != null && itemDto.getAvailable() != null) {
            item.setDescription(itemDto.getDescription());
            item.setAvailable(itemDto.getAvailable());
        } else if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        } else if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        } else if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return item;
    }
}