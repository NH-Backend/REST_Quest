package io.nh_backend.rest_quest.item.repository;

import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {
}
