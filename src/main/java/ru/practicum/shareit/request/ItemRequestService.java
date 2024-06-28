package ru.practicum.shareit.request;

import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswerDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addNewRequest(final Long userId, NewItemRequestDto requestDto);

    List<ItemRequestWithAnswerDto> getOwnUserItemRequests(final Long userId);

    List<ItemRequestWithAnswerDto> getUsersItemRequests(final Long userId, final PageRequest pageRequest);

    ItemRequestWithAnswerDto getRequestById(final Long userId, final Long requestId);

}
