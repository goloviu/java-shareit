package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {

    List<Item> findByOwner(final Long userId, final PageRequest page);

    @Query("SELECT i " +
            "FROM Item i " +
            "WHERE (upper(i.description) LIKE upper(concat('%', ?1, '%')) " +
            "OR upper(i.name) LIKE upper(concat('%', ?1, '%'))) " +
            "AND i.available = true")
    List<Item> findAvailableItemsByText(final String regEx, final Pageable page);

    void removeByIdAndOwner(final Long itemId, final Long userId);

    List<Item> findAllByRequestIdIn(final List<Long> requestId);
}
