package io.nh_backend.rest_quest.npc.repository;

import io.nh_backend.rest_quest.friend_request.domain.FriendRequest;
import io.nh_backend.rest_quest.npc.domain.Npc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NpcRepository extends JpaRepository<Npc, Long> {

    // INF_UNITY_025: 활성화된 NPC 목록 및 상점 아이템 전체 FETCH JOIN
    @Query("select distinct n from Npc n left join fetch n.shopItems si left join fetch si.item where n.active = true")
    List<Npc> findAllActiveNpcsWithShopItems();

    // INF_UNITY_026: 특정 NPC 단건 상세 조회 (FETCH JOIN)
    @Query("select distinct n from Npc n left join fetch n.shopItems si left join fetch si.item where n.id = :id")
    Optional<Npc> findNpcWithShopItemsById(@Param("id") Long id);

    Optional<Npc> findByRId(String rId);
}
