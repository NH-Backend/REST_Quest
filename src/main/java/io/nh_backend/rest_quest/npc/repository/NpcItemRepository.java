package io.nh_backend.rest_quest.npc.repository;

import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.npc.domain.Npc;
import io.nh_backend.rest_quest.npc.domain.NpcItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NpcItemRepository extends JpaRepository<NpcItem, Long> {
    Optional<NpcItem> findByIdAndNpc(Long id, Npc npc);
}
