package ru.practicum.shareit.integrtest.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@DataJpaTest
class ItemRepositoryTest {

    private Item item;
    private User user;
    private ItemRequest itemRequest;

    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    public ItemRepositoryTest(ItemRepository itemRepository, UserRepository userRepository,
                              ItemRequestRepository itemRequestRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .name("Test name")
                .email("teseemail@email.ru")
                .build();

        userRepository.save(user);

        itemRequest = ItemRequest.builder()
                .description("Test desc")
                .requestorId(user.getId())
                .created(LocalDateTime.now())
                .build();

        itemRequestRepository.save(itemRequest);

        item = Item.builder()
                .owner(user.getId())
                .name("ITEM TEST NAME")
                .description("ITEM TEST DESCRIPTION")
                .available(true)
                .build();

        itemRepository.save(item);
    }

    @AfterEach
    public void tearDown() {
        // Release test data after each test method
        itemRepository.delete(item);
        itemRequestRepository.delete(itemRequest);
        userRepository.delete(user);
    }

    @Test
    void testFindByOwner_ShouldRetunListItemsByOwnerId_WhenItemExists() {
        // given
        Long ownerId = user.getId();

        // do
        List<Item> result = itemRepository.findByOwner(ownerId, PageRequest.of(0, 2));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(item)));
    }

    @Test
    void testFindAvailableItemsByText_ShouldReturnListItemsByRegEx_WhenItemEsits() {
        // given
        String regEx = "ITEM TEST NAME";

        // do
        List<Item> result = itemRepository.findAvailableItemsByText(regEx, PageRequest.of(0, 2));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(item)));
    }

    @Test
    void testRemoveByIdAndOwner_ShouldRemoveItemByIdAndOwnerId_WhenItemExists() {
        // given
        Optional<Item> result = itemRepository.findById(item.getId());
        assertThat(result.isPresent(), equalTo(true));

        // do
        itemRepository.removeByIdAndOwner(item.getId(), user.getId());
        Optional<Item> expect = itemRepository.findById(item.getId());
        // expect
        assertThat(expect.isPresent(), equalTo(false));
    }

    @Test
    void testFindAllByRequestId_ShouldReturnListItemByRequestId_WhenItemExists() {
        // given
        Long requestId = itemRequest.getId();
        item.setRequest(itemRequest);

        // do
        List<Item> result = itemRepository.findAllByRequestIdIn(List.of(requestId));

        // expect
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(List.of(item)));
    }

    @Test
    void testSaveItem_ShouldReturnSavedItemWithItemRequestByItemAndRequestId_WhenItemIsNotNullAndItemRequestExists() {
        // given
        Long requestId = itemRequest.getId();
        Item itemForSave = Item.builder()
                .owner(user.getId())
                .name("ITEM2 TEST NAME")
                .description("ITEM2 TEST DESCRIPTION")
                .available(true)
                .build();

        // do
        Item result = itemRepository.saveItem(itemForSave, requestId);

        // expect
        assertThat(result, equalTo(itemForSave));
        assertThat(result.getRequest().getId(), equalTo(requestId));
    }
}