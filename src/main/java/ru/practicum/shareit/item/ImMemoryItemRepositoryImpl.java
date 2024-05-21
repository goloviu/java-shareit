package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
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
    public Item updateItem(final Item item) {
        if (item.getId() == null || !items.containsKey(item.getId())) {
            throw new IllegalArgumentException("Некорректно указан ID предмета: " + item.getId());
        }

        checkItemExist(item.getId());

        Item itemForUpdate = items.get(item.getId());

        Item itemBeforeUpdate = Item.builder()
                .id(itemForUpdate.getId())
                .owner(itemForUpdate.getOwner())
                .name(itemForUpdate.getName())
                .description(itemForUpdate.getDescription())
                .available(itemForUpdate.getAvailable())
                .request(itemForUpdate.getRequest())
                .build();

        updateFields(item);

        items.put(item.getId(), itemForUpdate);
        log.info("Предмет с ID {} успешно обновлен в БД. \nБыло: {} \nСтало: {}", item.getId(), itemBeforeUpdate,
                item);
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

    private Item updateFields(final Item item) {
        Item updateItem = items.get(item.getId());

        if (item.getName() != null && item.getDescription() != null && item.getAvailable() != null) {
            updateItem.setName(item.getName());
            updateItem.setDescription(item.getDescription());
            updateItem.setAvailable(item.getAvailable());
        } else if (item.getName() != null && item.getDescription() != null) {
            updateItem.setName(item.getName());
            updateItem.setDescription(item.getDescription());
        } else if (item.getName() != null && item.getAvailable() != null) {
            updateItem.setName(item.getName());
            updateItem.setAvailable(item.getAvailable());
        } else if (item.getDescription() != null && item.getAvailable() != null) {
            updateItem.setDescription(item.getDescription());
            updateItem.setAvailable(item.getAvailable());
        } else if (item.getName() != null) {
            updateItem.setName(item.getName());
        } else if (item.getDescription() != null) {
            updateItem.setDescription(item.getDescription());
        } else if (item.getAvailable() != null) {
            updateItem.setAvailable(item.getAvailable());
        }
        return updateItem;
    }
}