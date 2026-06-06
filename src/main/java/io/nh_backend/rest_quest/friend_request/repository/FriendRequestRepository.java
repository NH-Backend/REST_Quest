package io.nh_backend.rest_quest.friend_request.repository;

import io.nh_backend.rest_quest.friend_request.domain.FriendRequest;
import io.nh_backend.rest_quest.friend_request.domain.FriendStatus;
import io.nh_backend.rest_quest.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    @Query("""
            select count(friendRequest)
            from FriendRequest friendRequest
            where friendRequest.status = :status
              and friendRequest.deletedAt is null
              and (friendRequest.fromUser = :user or friendRequest.toUser = :user)
            """)
    Long countActiveFriends(
            @Param("user") User user,
            @Param("status") FriendStatus status
    );
}
