package io.nh_backend.rest_quest.npc.repository;

import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.npc.domain.Npc;
import io.nh_backend.rest_quest.npc.domain.NpcItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.Optional;

public interface NpcItemRepository extends JpaRepository<NpcItem, Long> {
    Optional<NpcItem> findByIdAndNpc(Long id, Npc npc);
    @Query("""
        select ni
        from NpcItem ni
        join fetch ni.npc n
        join fetch ni.item i
        where ni.id = :npcItemId
        and n.id = :npcId
    """)
    Optional<NpcItem> findPurchaseTarget(
            @Param("npcId") Long npcId,
            @Param("npcItemId") Long npcItemId
    );
}
