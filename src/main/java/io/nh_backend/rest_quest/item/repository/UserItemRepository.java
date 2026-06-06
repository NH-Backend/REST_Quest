package io.nh_backend.rest_quest.item.repository;

import io.nh_backend.rest_quest.item.domain.UserItem;
import io.nh_backend.rest_quest.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    List<UserItem> findAllByUserAndDeletedAtIsNull(User user);
}
