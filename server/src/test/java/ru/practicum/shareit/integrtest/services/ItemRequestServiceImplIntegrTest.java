package ru.practicum.shareit.integrtest.services;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswerDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(properties = "db.name=test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplIntegrTest {

    private final ItemRequestService itemRequestService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    private User user;
    private Item item;
    private Item item2;
    private ItemRequest itemRequest;
    private ItemRequest itemRequest2;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .name("Test name")
                .email("teseemail@email.ru")
                .build();

        userRepository.save(user);

        itemRequest = ItemRequest.builder()
                .created(LocalDateTime.now())
                .requestorId(user.getId())
                .description("TEST REQUEST TEXT")
                .build();

        itemRequest2 = ItemRequest.builder()
                .created(LocalDateTime.now())
                .requestorId(user.getId())
                .description("TEST2 REQUEST TEXT")
                .build();

        itemRequestRepository.save(itemRequest);
        itemRequestRepository.save(itemRequest2);

        item = Item.builder()
                .owner(user.getId())
                .name("ITEM TEST NAME")
                .description("ITEM TEST DESCRIPTION")
                .available(true)
                .request(itemRequest)
                .build();

        item2 = Item.builder()
                .owner(user.getId())
                .name("ITEM TEST NAME2")
                .description("ITEM TEST DESCRIPTION2")
                .available(true)
                .request(itemRequest2)
                .build();

        itemRepository.save(item);
        itemRepository.save(item2);
    }

    @AfterEach
    public void tearDown() {
        // Release test data after each test method
        itemRepository.deleteById(item.getId());
        itemRepository.deleteById(item2.getId());
        itemRequestRepository.delete(itemRequest);
        itemRequestRepository.delete(itemRequest2);
        userRepository.deleteById(user.getId());
    }

    @Test
    void testGetOwnUserItemRequests_ShouldReturnListItemRequestWithAnswerDto_WhenRequestsExsitsAndItemExists() {
        // do
        List<ItemRequestWithAnswerDto> result = itemRequestService.getOwnUserItemRequests(user.getId());

        // expect
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getCreated(), notNullValue());
        assertThat(result.get(0).getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(result.get(0).getItems(), notNullValue());
        assertThat(result.get(1).getCreated(), notNullValue());
        assertThat(result.get(1).getDescription(), equalTo(itemRequest2.getDescription()));
        assertThat(result.get(1).getItems(), notNullValue());
    }
}