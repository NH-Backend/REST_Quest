package io.nh_backend.rest_quest.item.repository;

import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.UserItem;
import io.nh_backend.rest_quest.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    // 특정 사용자의 가방 목록 중 버려지지 않은(정상 소유 중인) 아이템 목록만 조회
    List<UserItem> findAllByUserAndDeletedAtIsNull(User user);

    // 아이템 중복 지급 방지용: 가방에 똑같은 아이템을 이미 가지고 있는지 검사
    Optional<UserItem> findByUserAndItemAndDeletedAtIsNull(User user, Item item);

    // 아이템 소모/버리기 연산 시 단건 검증을 위한 조회
    Optional<UserItem> findByIdAndUserAndDeletedAtIsNull(Long id, User user);
}
