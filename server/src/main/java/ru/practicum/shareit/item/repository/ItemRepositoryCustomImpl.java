package ru.practicum.shareit.item.repository;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Transactional
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Item saveItem(Item item, Long requestId) {
        if (requestId != null) {
            item.setRequest(entityManager.getReference(ItemRequest.class, requestId));
        }
        entityManager.persist(item);
        entityManager.flush();

        return item;
    }
}
