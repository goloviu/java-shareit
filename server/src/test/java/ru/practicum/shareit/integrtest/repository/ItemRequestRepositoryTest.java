package ru.practicum.shareit.integrtest.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
class ItemRequestRepositoryTest {

    private UserRepository userRepository;
    private ItemRequestRepository itemRequestRepository;

    private User testUser;
    private User testUser2;
    private ItemRequest testItemRequest;

    @Autowired
    public ItemRequestRepositoryTest(ItemRequestRepository itemRequestRepository, UserRepository userRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
    }

    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .name("Test name")
                .email("teseemail@email.ru")
                .build();

        testUser2 = User.builder()
                .name("Test2 name")
                .email("teseemail2@email.ru")
                .build();

        userRepository.save(testUser);
        userRepository.save(testUser2);

        testItemRequest = ItemRequest.builder()
                .description("Test desc")
                .requestorId(testUser.getId())
                .created(LocalDateTime.now())
                .build();

        itemRequestRepository.save(testItemRequest);
    }

    @AfterEach
    public void tearDown() {
        // Release test data after each test method
        userRepository.delete(testUser);
        userRepository.delete(testUser2);
        itemRequestRepository.delete(testItemRequest);
    }

    @Test
    void testFindAllByRequestorIdOrderByCreatedAsc_ShouldReturnRequestsByRequestorId_WhenRequestsExists() {
        // given
        ItemRequest itemRequest = ItemRequest.builder()
                .description("Test2 desc")
                .requestorId(testUser.getId())
                .created(LocalDateTime.now().plusHours(5))
                .build();

        itemRequestRepository.save(itemRequest);
        // do
        List<ItemRequest> result = itemRequestRepository.findAllByRequestorIdOrderByCreatedAsc(testUser.getId());

        // expect
        assertThat(result, hasSize(2));
        assertThat(result, equalTo(List.of(testItemRequest, itemRequest)));
    }

    @Test
    void testFindAllByRequestorIdNot_ShouldReturnRequestByNotRequestorId1_WhenOtherRequestExists() {
        // given
        ItemRequest itemRequest = ItemRequest.builder()
                .description("Test2 desc")
                .requestorId(testUser2.getId())
                .created(LocalDateTime.now().plusHours(5))
                .build();

        itemRequestRepository.save(itemRequest);
        // do
        Page<ItemRequest> result = itemRequestRepository.findAllByRequestorIdNot(testUser.getId(),
                PageRequest.of(0, 2));

        // expect
        assertThat(result.getTotalElements(), equalTo(1L));
        assertThat(result.getContent(), equalTo(List.of(itemRequest)));
    }
}