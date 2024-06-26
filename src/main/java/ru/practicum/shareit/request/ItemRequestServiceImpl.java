package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswerDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private UserRepository userStorage;
    private ItemRequestRepository itemRequestStorage;
    private ItemRepository itemStorage;

    @Autowired
    public ItemRequestServiceImpl(UserRepository userStorage, ItemRequestRepository itemRequestStorage,
                                  ItemRepository itemStorage) {
        this.userStorage = userStorage;
        this.itemRequestStorage = itemRequestStorage;
        this.itemStorage = itemStorage;
    }

    @Override
    public ItemRequestDto addNewRequest(Long userId, NewItemRequestDto requestDto) {
        checkUserExists(userId);

        ItemRequest newRequest = ItemRequestMapper.newItemRequestDtoToItemRequest(requestDto, userId);
        ItemRequest savedRequest = itemRequestStorage.save(newRequest);
        log.info("В БД сохранен новый запрос: \n {}", savedRequest);
        return ItemRequestMapper.itemRequestToItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestWithAnswerDto> getOwnUserItemRequests(Long userId) {
        checkUserExists(userId);

        List<ItemRequest> requests = itemRequestStorage.findAllByRequestorIdOrderByCreatedAsc(userId);
        List<ItemRequestWithAnswerDto> resultRequests = new ArrayList<>();

        if (!requests.isEmpty()) {
            for (ItemRequest request : requests) {
                List<Item> items = itemStorage.findAllByRequestId(request.getId());
                ItemRequestWithAnswerDto requestDto = ItemRequestMapper
                        .itemRequestToItemRequestWithAnswerDto(request, items);
                resultRequests.add(requestDto);
            }
        }
        log.info("Получены запросы на вещи с ответами из БД. \n {}", resultRequests);
        return resultRequests;
    }

    @Override
    public List<ItemRequestWithAnswerDto> getUsersItemRequests(Long userId, PageRequest pageRequest) {
        checkUserExists(userId);

        Page<ItemRequest> requests = itemRequestStorage.findAllByRequestorIdNot(userId, pageRequest);
        List<ItemRequestWithAnswerDto> resultRequests = new ArrayList<>();

        if (!requests.isEmpty()) {
            for (ItemRequest request : requests) {
                List<Item> items = itemStorage.findAllByRequestId(request.getId());
                ItemRequestWithAnswerDto requestDto = ItemRequestMapper
                        .itemRequestToItemRequestWithAnswerDto(request, items);
                resultRequests.add(requestDto);
            }
        }
        log.info("Получены запросы на вещи с ответами из БД. \n {}", resultRequests);
        return resultRequests;
    }

    @Override
    public ItemRequestWithAnswerDto getRequestById(Long userId, Long requestId) {
        checkUserExists(userId);

        ItemRequest request = itemRequestStorage.findById(requestId)
                .orElseThrow(() -> new ItemRequestNotFoundException("Запрос на вещь по указанному ID " + requestId
                        + " не найден"));
        List<Item> items = itemStorage.findAllByRequestId(request.getId());

        return ItemRequestMapper.itemRequestToItemRequestWithAnswerDto(request, items);
    }

    private void checkUserExists(final Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new UserNotFoundException("Пользователь не найден по ID " + userId);
        }
    }
}
