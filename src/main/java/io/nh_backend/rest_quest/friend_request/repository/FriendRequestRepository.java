package io.nh_backend.rest_quest.friend_request.repository;

import io.nh_backend.rest_quest.friend_request.domain.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
}
