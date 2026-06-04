package io.nh_backend.rest_quest.npc.repository;

import io.nh_backend.rest_quest.friend_request.domain.FriendRequest;
import io.nh_backend.rest_quest.npc.domain.Npc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NpcRepository extends JpaRepository<Npc, Long> {
}
