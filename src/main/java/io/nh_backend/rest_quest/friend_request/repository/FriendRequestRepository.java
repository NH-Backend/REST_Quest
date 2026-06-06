package io.nh_backend.rest_quest.friend_request.repository;

import io.nh_backend.rest_quest.friend_request.domain.FriendRequest;
import io.nh_backend.rest_quest.friend_request.domain.FriendStatus;
import io.nh_backend.rest_quest.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    @Query("""
            select friendRequest
            from FriendRequest friendRequest
            join fetch friendRequest.fromUser
            join fetch friendRequest.toUser
            where friendRequest.status = :status
              and friendRequest.deletedAt is null
              and (friendRequest.fromUser = :user or friendRequest.toUser = :user)
            order by friendRequest.createdAt desc
            """)
    List<FriendRequest> findAcceptedFriends(
            @Param("user") User user,
            @Param("status") FriendStatus status
    );

    List<FriendRequest> findAllByToUserAndStatusAndDeletedAtIsNull(User toUser, FriendStatus status);

    @Query("""
            select count(friendRequest) > 0
            from FriendRequest friendRequest
            where friendRequest.deletedAt is null
              and (
                    (friendRequest.fromUser = :firstUser and friendRequest.toUser = :secondUser)
                    or (friendRequest.fromUser = :secondUser and friendRequest.toUser = :firstUser)
              )
            """)
    boolean existsActiveRelationBetween(
            @Param("firstUser") User firstUser,
            @Param("secondUser") User secondUser
    );
}
