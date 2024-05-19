package ru.practicum.javashareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.javashareit.exceptions.PermissionException;
import ru.practicum.javashareit.item.dto.ItemDto;
import ru.practicum.javashareit.item.dto.ItemMapper;
import ru.practicum.javashareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemStorage;
    private final UserRepository userStorage;

    @Autowired
    public ItemServiceImpl(ItemRepository itemStorage, UserRepository userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public List<ItemDto> getOwnerItems(final Long userId) {
        List<Item> items = itemStorage.findItemsByUserId(userId);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(final Long itemId) {
        return ItemMapper.toItemDto(itemStorage.getItemById(itemId));
    }

    @Override
    public List<ItemDto> findItemsByText(String regEx) {
        List<Item> items = itemStorage.findItemsByText(regEx);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto addNewItem(final Long userId, final Item item) {
        userStorage.checkUserExist(userId);

        item.setOwner(userId);
        return ItemMapper.toItemDto(itemStorage.save(item));
    }

    @Override
    public ItemDto updateItem(final Long userId, final Long itemId, final ItemDto itemDto) {
        Item item = itemStorage.getItemById(itemId);
        if (!item.getOwner().equals(userId)) {
            throw new PermissionException("Недостаточно прав для изменения вещи у пользователя ID " + userId + " " +
                    "изменить вещь может только владелец");
        }
        itemDto.setOwner(userId);
        itemDto.setId(itemId);
        return ItemMapper.toItemDto(itemStorage.updateItem(itemDto));
    }

    @Override
    public void deleteItem(final Long userId, final Long itemId) {
        itemStorage.deleteByUserIdAndItemId(userId, itemId);
    }
}
