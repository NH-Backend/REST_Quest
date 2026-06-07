package io.nh_backend.rest_quest.friend_request.repository;

import io.nh_backend.rest_quest.friend_request.domain.FriendRequest;
import io.nh_backend.rest_quest.friend_request.domain.FriendStatus;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=USER")
class FriendRequestRepositoryTest {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("삭제된 친구 관계는 fromUser와 toUser 양쪽 친구 목록에서 조회되지 않는다")
    void findAcceptedFriends_excludesDeletedRelationForBothUsers() {
        //given
        User fromUser = saveUser("from@test.com", "보낸유저");
        User toUser = saveUser("to@test.com", "받은유저");
        FriendRequest friendRequest = friendRequestRepository.save(FriendRequest.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .status(FriendStatus.ACCEPTED)
                .build());

        friendRequest.delete();
        entityManager.flush();
        entityManager.clear();

        //when
        var fromUserFriends = friendRequestRepository.findAcceptedFriends(fromUser, FriendStatus.ACCEPTED);
        var toUserFriends = friendRequestRepository.findAcceptedFriends(toUser, FriendStatus.ACCEPTED);

        //then
        assertThat(fromUserFriends).isEmpty();
        assertThat(toUserFriends).isEmpty();
    }

    @Test
    @DisplayName("ACCEPTED 친구 관계는 fromUser와 toUser 어느 쪽에서도 같은 관계로 찾을 수 있다")
    void findAcceptedRelationsBetween_findsRelationFromBothDirections() {
        //given
        User fromUser = saveUser("from-direction@test.com", "보낸유저");
        User toUser = saveUser("to-direction@test.com", "받은유저");
        FriendRequest friendRequest = friendRequestRepository.save(FriendRequest.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .status(FriendStatus.ACCEPTED)
                .build());

        entityManager.flush();
        entityManager.clear();

        //when
        var foundByFromUser = friendRequestRepository.findAcceptedRelationsBetween(
                fromUser,
                toUser.getId(),
                FriendStatus.ACCEPTED
        );
        var foundByToUser = friendRequestRepository.findAcceptedRelationsBetween(
                toUser,
                fromUser.getId(),
                FriendStatus.ACCEPTED
        );

        //then
        assertThat(foundByFromUser)
                .extracting(FriendRequest::getId)
                .containsExactly(friendRequest.getId());
        assertThat(foundByToUser)
                .extracting(FriendRequest::getId)
                .containsExactly(friendRequest.getId());
    }

    @Test
    @DisplayName("중복된 양방향 ACCEPTED 친구 관계를 모두 찾을 수 있다")
    void findAcceptedRelationsBetween_findsDuplicatedRelationsInBothDirections() {
        //given
        User fromUser = saveUser("from-duplicate@test.com", "보낸유저");
        User toUser = saveUser("to-duplicate@test.com", "받은유저");
        FriendRequest sentRelation = friendRequestRepository.save(FriendRequest.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .status(FriendStatus.ACCEPTED)
                .build());
        FriendRequest receivedRelation = friendRequestRepository.save(FriendRequest.builder()
                .fromUser(toUser)
                .toUser(fromUser)
                .status(FriendStatus.ACCEPTED)
                .build());

        entityManager.flush();
        entityManager.clear();

        //when
        var foundRelations = friendRequestRepository.findAcceptedRelationsBetween(
                toUser,
                fromUser.getId(),
                FriendStatus.ACCEPTED
        );

        //then
        assertThat(foundRelations)
                .extracting(FriendRequest::getId)
                .containsExactlyInAnyOrder(sentRelation.getId(), receivedRelation.getId());
    }

    @Test
    @DisplayName("DECLINED 상태의 친구 요청은 활성 관계 조회에서 제외된다")
    void existsActiveRelationBetween_excludesDeclinedStatus() {
        //given
        User fromUser = saveUser("from-declined@test.com", "보낸유저");
        User toUser = saveUser("to-declined@test.com", "받은유저");
        friendRequestRepository.save(FriendRequest.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .status(FriendStatus.DECLINED)
                .build());

        entityManager.flush();
        entityManager.clear();

        //when
        boolean exists = friendRequestRepository.existsActiveRelationBetween(fromUser, toUser);

        //then
        assertThat(exists).isFalse();
    }

    private User saveUser(String email, String nickname) {
        User user = User.builder()
                .email(email)
                .password("encoded-password")
                .nickname(nickname)
                .role(Role.USER)
                .build();
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }
}
