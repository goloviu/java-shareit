package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.PermissionException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemRegisterDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

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
                .map(ItemMapper::itemToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(final Long itemId) {
        return ItemMapper.itemToItemDto(itemStorage.getItemById(itemId));
    }

    @Override
    public List<ItemDto> findItemsByText(String regEx) {
        List<Item> items = itemStorage.findItemsByText(regEx);
        return items.stream()
                .map(ItemMapper::itemToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto addNewItem(final Long userId, final ItemRegisterDto itemRegisterDto) {
        userStorage.checkUserExist(userId);

        itemRegisterDto.setOwner(userId);
        return ItemMapper.itemToItemDto(itemStorage.save(ItemMapper.itemRegisterDtoToItem(itemRegisterDto)));
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
        return ItemMapper.itemToItemDto(itemStorage.updateItem(ItemMapper.itemDtoToItem(itemDto)));
    }

    @Override
    public void deleteItem(final Long userId, final Long itemId) {
        itemStorage.deleteByUserIdAndItemId(userId, itemId);
    }
}