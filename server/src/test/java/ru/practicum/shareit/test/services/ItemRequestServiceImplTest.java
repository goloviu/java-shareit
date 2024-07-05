package ru.practicum.shareit.test.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.practicum.shareit.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswerDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private UserRepository userStorage;
    @Mock
    private ItemRequestRepository itemRequestStorage;
    @Mock
    private ItemRepository itemStorage;
    private ItemRequestService itemRequestService;

    @BeforeEach
    private void setUp() {
        this.itemRequestService = new ItemRequestServiceImpl(userStorage, itemRequestStorage, itemStorage);
    }

    @Test
    void testAddNewRequest_ShouldSaveNewRequest_WhenRequestIsNotNull() {
        // given
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Test Desc")
                .created(LocalDateTime.now())
                .requestorId(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@mail.ru")
                .name("Joe")
                .build();

        when(itemRequestStorage.save(isA(ItemRequest.class)))
                .thenReturn(request);
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);

        // do
        ItemRequestDto result = itemRequestService.addNewRequest(user.getId(),
                new NewItemRequestDto("Test Desc"));
        ItemRequestDto expect = ItemRequestMapper.itemRequestToItemRequestDto(request);

        // expect
        assertThat(expect, equalTo(result));
        verify(itemRequestStorage, times(1))
                .save(isA(ItemRequest.class));
        verify(userStorage, times(1))
                .existsById(anyLong());
        verifyNoMoreInteractions(userStorage, itemRequestStorage);
    }

    @Test
    void testAddNewRequest_ShouldThrowUserNotFoundException_WhenUserIsNotExists() {
        // given
        User user = User.builder()
                .id(1L)
                .email("test@mail.ru")
                .name("Joe")
                .build();

        when(userStorage.existsById(1L))
                .thenThrow(new UserNotFoundException("Пользователь не найден по ID " + user.getId()));

        // expect
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> itemRequestService.addNewRequest(user.getId(), new NewItemRequestDto("Test Desc")));

        verify(userStorage, times(1))
                .existsById(1L);
        verifyNoMoreInteractions(userStorage, itemRequestStorage, itemStorage);
        assertThat(exception.getMessage(), equalTo("Пользователь не найден по ID " + user.getId()));
    }

    @Test
    void testGetOwnUserItemRequests_ShouldReturnOwnnerItemRequestWithAnswerDto_WhenRequestsExists() {
        // given
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Test Desc")
                .created(LocalDateTime.now())
                .requestorId(1L)
                .build();

        Item item = Item.builder()
                .id(1L)
                .owner(1L)
                .available(true)
                .description("Test item desc")
                .name("Test item name")
                .request(request)
                .build();

        when(itemRequestStorage.findAllByRequestorIdOrderByCreatedAsc(anyLong()))
                .thenReturn(List.of(request));
        when(itemStorage.findAllByRequestIdIn(anyList()))
                .thenReturn(List.of(item));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);

        // do
        List<ItemRequestWithAnswerDto> result = itemRequestService.getOwnUserItemRequests(anyLong());
        List<ItemRequestWithAnswerDto> expect = List.of(
                ItemRequestMapper
                        .itemRequestToItemRequestWithAnswerDto(request, List.of(item)));

        // expect
        assertThat(expect, equalTo(result));
        verify(itemRequestStorage, times(1))
                .findAllByRequestorIdOrderByCreatedAsc(anyLong());
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(itemStorage, times(1))
                .findAllByRequestIdIn(anyList());
        verifyNoMoreInteractions(userStorage, itemRequestStorage, itemStorage);
    }

    @Test
    void testGetOwnUserItemRequests_ShouldThrowUserNotFoundException_WhenUserNotExists() {
        // given
        Long userId = 1L;

        when(userStorage.existsById(1L))
                .thenThrow(new UserNotFoundException("Пользователь не найден по ID " + userId));

        // expect
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> itemRequestService.getOwnUserItemRequests(userId));

        verify(userStorage, times(1))
                .existsById(1L);
        verifyNoMoreInteractions(userStorage);
        assertThat(exception.getMessage(), equalTo("Пользователь не найден по ID " + userId));
    }

    @Test
    void testGetUsersItemRequests_ShouldReturnOtherUsersItemRequestWithAnswerDto_WhenRequestExists() {
        // given
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Test Desc")
                .created(LocalDateTime.now())
                .requestorId(2L)
                .build();

        Item item = Item.builder()
                .id(1L)
                .owner(3L)
                .available(true)
                .description("Test item desc")
                .name("Test item name")
                .request(request)
                .build();

        Long userId = 1L;

        Page<ItemRequest> requests = new PageImpl<>(List.of(request),
                Pageable.ofSize(1), 0);

        when(itemRequestStorage.findAllByRequestorIdNot(anyLong(), isA(PageRequest.class)))
                .thenReturn(requests);
        when(itemStorage.findAllByRequestIdIn(anyList()))
                .thenReturn(List.of(item));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);

        // do
        List<ItemRequestWithAnswerDto> result = itemRequestService.getUsersItemRequests(userId,
                PageRequest.of(0, 1, Sort.unsorted()));
        List<ItemRequestWithAnswerDto> expect = List.of(
                ItemRequestMapper
                        .itemRequestToItemRequestWithAnswerDto(request, List.of(item)));

        // expect
        assertThat(expect, equalTo(result));
        assertThat(userId, not(request.getRequestorId()));
        verify(itemRequestStorage, times(1))
                .findAllByRequestorIdNot(anyLong(), isA(PageRequest.class));
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(itemStorage, times(1))
                .findAllByRequestIdIn(anyList());
        verifyNoMoreInteractions(userStorage, itemRequestStorage, itemStorage);
    }

    @Test
    void testGetUsersItemRequests_ShouldThrowUserNotFoundException_WhenUserNotExists() {
        // given
        Long userId = 1L;

        when(userStorage.existsById(userId))
                .thenThrow(new UserNotFoundException("Пользователь не найден по ID " + userId));

        // expect
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> itemRequestService.getUsersItemRequests(userId, PageRequest.of(1, 1, Sort.unsorted())));

        verify(userStorage, times(1))
                .existsById(1L);
        verifyNoMoreInteractions(userStorage);
        assertThat(exception.getMessage(), equalTo("Пользователь не найден по ID " + userId));
    }

    @Test
    void testGetRequestById_ShouldReturnItemRequestWithAnswerDto_WhenRequestExists() {
        // given
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Test Desc")
                .created(LocalDateTime.now())
                .requestorId(2L)
                .build();

        Item item = Item.builder()
                .id(1L)
                .owner(3L)
                .available(true)
                .description("Test item desc")
                .name("Test item name")
                .request(request)
                .build();

        Long userId = 1L;

        when(itemRequestStorage.findById(anyLong()))
                .thenReturn(Optional.of(request));
        when(itemStorage.findAllByRequestIdIn(anyList()))
                .thenReturn(List.of(item));
        when(userStorage.existsById(anyLong()))
                .thenReturn(true);

        // do
        ItemRequestWithAnswerDto result = itemRequestService.getRequestById(userId, request.getId());
        ItemRequestWithAnswerDto expect = ItemRequestMapper.itemRequestToItemRequestWithAnswerDto(request, List.of(item));

        // expect
        assertThat(expect, equalTo(result));
        verify(itemRequestStorage, times(1))
                .findById(anyLong());
        verify(userStorage, times(1))
                .existsById(anyLong());
        verify(itemStorage, times(1))
                .findAllByRequestIdIn(anyList());
        verifyNoMoreInteractions(userStorage, itemRequestStorage, itemStorage);
    }

    @Test
    void testGetRequestById_ShouldThrowUserNotFoundException_WhenUserNotExists() {
        // given
        Long userId = 1L;

        when(userStorage.existsById(userId))
                .thenThrow(new UserNotFoundException("Пользователь не найден по ID " + userId));

        // expect
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> itemRequestService.getRequestById(userId, 1L));

        verify(userStorage, times(1))
                .existsById(1L);
        assertThat(exception.getMessage(), equalTo("Пользователь не найден по ID " + userId));
        verifyNoMoreInteractions(userStorage);
    }

    @Test
    void testGetRequestById_ShouldThrowItemRequestNotFoundException_WhenRequestNotExists() {
        // given
        Long userId = 1L;
        Long requestId = 1L;

        when(userStorage.existsById(userId))
                .thenReturn(true);
        when(itemRequestStorage.findById(requestId))
                .thenThrow(new ItemRequestNotFoundException("Запрос на вещь по указанному ID " + requestId
                        + " не найден"));

        // expect
        final ItemRequestNotFoundException exception = Assertions.assertThrows(
                ItemRequestNotFoundException.class,
                () -> itemRequestService.getRequestById(userId, requestId));

        verify(userStorage, times(1))
                .existsById(1L);
        verify(itemRequestStorage, times(1))
                .findById(1L);
        assertThat(exception.getMessage(), equalTo("Запрос на вещь по указанному ID " + requestId
                + " не найден"));
        verifyNoMoreInteractions(userStorage, itemRequestStorage);
    }
}