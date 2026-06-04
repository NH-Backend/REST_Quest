package io.nh_backend.rest_quest.item.repository;

import io.nh_backend.rest_quest.friend_request.domain.FriendRequest;
import io.nh_backend.rest_quest.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
