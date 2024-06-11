package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemStorage;
    private final UserRepository userStorage;
    private final BookingRepository bookingStorage;
    private final CommentRepository commentStorage;

    @Autowired
    public ItemServiceImpl(ItemRepository itemStorage, UserRepository userStorage, BookingRepository bookingStorage, CommentRepository commentStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
        this.bookingStorage = bookingStorage;
        this.commentStorage = commentStorage;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemWithBookingDto> getOwnerItemsWithBookings(final Long userId) {
        List<Item> items = itemStorage.findByOwner(userId);

        log.info("Сервис обработал получение предметов из базы. Результат: \n {}", items);
        List<ItemWithBookingDto> itemDtos = new ArrayList<>();

        for (Item item : items) {
            ItemWithBookingDto itemDto = getItemWitBookingDto(item);
            itemDto.setComments(getCommentsDtoByItemId(item.getId()));
            itemDtos.add(itemDto);
        }
        return itemDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemWithBookingDto getItemByIdWithBooking(final Long userId, final Long itemId) {
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Предмет не найден по ID " + itemId));

        log.info("Сервис обрабатывает запрос на получение предмета по ID {}, пользователем ID {}, \n {}", itemId,
                userId);

        if (item.getOwner().equals(userId)) {
            ItemWithBookingDto itemDto = getItemWitBookingDto(item);
            itemDto.setComments(getCommentsDtoByItemId(itemId));
            return itemDto;
        }

        ItemWithBookingDto itemDto = ItemMapper.itemToItemWithBookingDto(item);
        itemDto.setComments(getCommentsDtoByItemId(itemId));
        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> findItemsByText(String regEx) {
        if (regEx == null || regEx.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> items = itemStorage.findAvailableItemsByText(regEx);
        log.info("Сервис обработал запрос на нахождение предметов по ключевому слову \'{}\', \n {}", regEx, items);
        return items.stream()
                .map(ItemMapper::itemToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto addNewItem(final Long userId, final ItemRegisterDto itemRegisterDto) {
        if (!userStorage.existsById(userId)) {
            throw new UserNotFoundException("Пользователя по ID " + userId + " не существует");
        }

        Item registredItem = ItemMapper.itemRegisterDtoToItem(itemRegisterDto);
        registredItem.setOwner(userId);

        if (itemStorage.exists(Example.of(registredItem))) {
            throw new IllegalArgumentException("Вещь уже существует");
        } else if (registredItem.getId() != null) {
            throw new IllegalArgumentException("У новой вещи не должен быть указан ID");
        }

        Item item = itemStorage.save(registredItem);
        log.info("Сервис обработал запрос на сохранение нового предмета пользователя ID {}, с полученными данными: {}" +
                " Результат: \n {}", userId, itemRegisterDto, item);
        return ItemMapper.itemToItemDto(item);
    }

    @Override
    public ItemDto updateItem(final Long userId, final Long itemId, final ItemDto itemDto) {
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Предмет не найден по ID " + itemId));

        if (!item.getOwner().equals(userId)) {
            throw new PermissionException("Недостаточно прав для изменения вещи у пользователя ID " + userId + " " +
                    "изменить вещь может только владелец");
        }

        Item updatedItem = itemStorage.save(updateFields(itemDto, item));
        log.info("Сервис обработал запрос на обновление предмета пользователя ID {}, с полученными данными: {}" +
                " Результат: \n {}", userId, itemDto, updatedItem);
        return ItemMapper.itemToItemDto(updatedItem);
    }

    @Override
    public void deleteItem(final Long userId, final Long itemId) {
        itemStorage.removeByIdAndOwner(userId, itemId);
        log.info("Сервис обработал запрос на удаление предмета ID {}, пользователя ID {}", itemId, userId);
    }

    @Override
    public CommentDto addNewComment(final Long userId, final Long itemId, final CommentAddDto commentAddDto) {
        Booking booking = bookingStorage.findFirstByItemId(itemId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено по ID вещи " + itemId));

        if (!booking.getBooker().getId().equals(userId) || !booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new CommentException("Вы не можете оставить комментарий так как вы не бронировали эту вещь либо" +
                    " бронирование еще не закончено");
        }

        Comment comment = Comment.builder()
                .text(commentAddDto.getText())
                .item(booking.getItem())
                .author(booking.getBooker())
                .created(LocalDateTime.now())
                .build();
        commentStorage.save(comment);
        return ItemMapper.commentToCommentDto(comment);
    }

    private List<CommentDto> getCommentsDtoByItemId(final Long itemId) {
        return commentStorage.findByItemId(itemId).stream()
                .map(ItemMapper::commentToCommentDto)
                .collect(Collectors.toList());
    }

    private ItemWithBookingDto getItemWitBookingDto(final Item item) {
        LocalDateTime currentTime = LocalDateTime.now();
        List<Booking> lastBookingList = bookingStorage.findLastBooking(item.getId(), currentTime);
        List<Booking> nextBookingList = bookingStorage.findNextBooking(item.getId(), currentTime);

        if (!lastBookingList.isEmpty() && !nextBookingList.isEmpty()) {
            Booking lastBooking = lastBookingList.get(0);
            Booking nextBooking = nextBookingList.get(0);
            ItemWithBookingDto itemDto = ItemMapper.itemToItemWithBookingDto(item, lastBooking,
                    nextBooking);
            log.info("Получен предмет владельца с прошлой и следующим бронированием. \n {}", itemDto);
            return itemDto;
        } else if (!lastBookingList.isEmpty()) {
            Booking lastBooking = lastBookingList.get(0);
            ItemWithBookingDto itemDto = ItemMapper.itemToItemWithLastBookingDto(item, lastBooking);
            log.info("Получен предмет владельца с прошлым бронированием. \n {}", itemDto);
            return itemDto;
        } else if (!nextBookingList.isEmpty()) {
            Booking nextBooking = nextBookingList.get(0);
            ItemWithBookingDto itemDto = ItemMapper.itemToItemWithNextBookingDto(item, nextBooking);
            log.info("Получен предмет владельца с прошлым бронированием. \n {}", itemDto);
            return itemDto;
        }
        return ItemMapper.itemToItemWithBookingDto(item);
    }

    private Item updateFields(final ItemDto itemDto, final Item itemForUpdate) {

        if (itemDto.getName() != null && itemDto.getDescription() != null && itemDto.getAvailable() != null) {
            itemForUpdate.setName(itemDto.getName());
            itemForUpdate.setDescription(itemDto.getDescription());
            itemForUpdate.setAvailable(itemDto.getAvailable());
        } else if (itemDto.getName() != null && itemDto.getDescription() != null) {
            itemForUpdate.setName(itemDto.getName());
            itemForUpdate.setDescription(itemDto.getDescription());
        } else if (itemDto.getName() != null && itemDto.getAvailable() != null) {
            itemForUpdate.setName(itemDto.getName());
            itemForUpdate.setAvailable(itemDto.getAvailable());
        } else if (itemDto.getDescription() != null && itemDto.getAvailable() != null) {
            itemForUpdate.setDescription(itemDto.getDescription());
            itemForUpdate.setAvailable(itemDto.getAvailable());
        } else if (itemDto.getName() != null) {
            itemForUpdate.setName(itemDto.getName());
        } else if (itemDto.getDescription() != null) {
            itemForUpdate.setDescription(itemDto.getDescription());
        } else if (itemDto.getAvailable() != null) {
            itemForUpdate.setAvailable(itemDto.getAvailable());
        }
        return itemForUpdate;
    }
}