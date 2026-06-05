package io.nh_backend.rest_quest.item.repository;

import io.nh_backend.rest_quest.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByRId(String rId);
}
