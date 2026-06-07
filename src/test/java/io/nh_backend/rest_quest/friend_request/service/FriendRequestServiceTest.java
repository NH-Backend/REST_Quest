package io.nh_backend.rest_quest.friend_request.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.friend_request.domain.FriendRequest;
import io.nh_backend.rest_quest.friend_request.domain.FriendStatus;
import io.nh_backend.rest_quest.friend_request.dto.FriendRequestCreateRequest;
import io.nh_backend.rest_quest.friend_request.dto.FriendResponse;
import io.nh_backend.rest_quest.friend_request.repository.FriendRequestRepository;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FriendRequestServiceTest {

    private FriendRequestRepository friendRequestRepository;
    private UserRepository userRepository;
    private FriendRequestService friendRequestService;

    @BeforeEach
    void setUp() {
        friendRequestRepository = mock(FriendRequestRepository.class);
        userRepository = mock(UserRepository.class);
        friendRequestService = new FriendRequestService(friendRequestRepository, userRepository);
    }

    @Nested
    @DisplayName("친구 목록 조회")
    class 친구_목록_조회_테스트 {

        @Test
        @DisplayName("INF_UNITY_010: ACCEPTED 상태인 친구 요청을 상대방 닉네임과 함께 반환한다")
        void getAcceptedFriends_returnsAcceptedFriendsWithCounterpartNickname() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User friend = createUser(8L, "friend@test.com", "친구닉네임");
            FriendRequest friendRequest = createFriendRequest(1L, me, friend, FriendStatus.ACCEPTED,
                    LocalDateTime.of(2025, 1, 10, 12, 0));

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findAcceptedFriends(me, FriendStatus.ACCEPTED))
                    .thenReturn(List.of(friendRequest));

            List<FriendResponse> responses = friendRequestService.getAcceptedFriends(me.getEmail());

            //then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).friendRequestId()).isEqualTo(1L);
            assertThat(responses.get(0).fromUserId()).isEqualTo(5L);
            assertThat(responses.get(0).toUserId()).isEqualTo(8L);
            assertThat(responses.get(0).status()).isEqualTo("ACCEPTED");
            assertThat(responses.get(0).createdAt()).isEqualTo("2025-01-10T12:00:00");
            assertThat(responses.get(0).nickname()).isEqualTo("친구닉네임");
            verify(friendRequestRepository).findAcceptedFriends(me, FriendStatus.ACCEPTED);
        }

        @Test
        @DisplayName("인증 사용자를 찾을 수 없으면 Unauthorized 예외가 발생한다")
        void getAcceptedFriends_throwsUnauthorizedWhenUserDoesNotExist() {
            //given
            String email = "unknown@test.com";

            //when
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            //then
            assertThatThrownBy(() -> friendRequestService.getAcceptedFriends(email))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> {
                        BusinessException businessException = (BusinessException) exception;
                        assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_USER);
                    });
        }
    }

    @Nested
    @DisplayName("받은 친구 요청 목록 조회")
    class 받은_친구_요청_목록_조회_테스트 {

        @Test
        @DisplayName("INF_UNITY_011: 내게 온 PENDING 친구 요청을 요청자 닉네임과 함께 반환한다")
        void getPendingRequestsToMe_returnsPendingRequestsWithSenderNickname() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User sender = createUser(3L, "sender@test.com", "요청보낸유저");
            FriendRequest friendRequest = createFriendRequest(2L, sender, me, FriendStatus.PENDING,
                    LocalDateTime.of(2026, 5, 21, 9, 0));

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findAllByToUserAndStatusAndDeletedAtIsNull(me, FriendStatus.PENDING))
                    .thenReturn(List.of(friendRequest));

            List<FriendResponse> responses = friendRequestService.getPendingRequestsToMe(me.getEmail());

            //then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).friendRequestId()).isEqualTo(2L);
            assertThat(responses.get(0).fromUserId()).isEqualTo(3L);
            assertThat(responses.get(0).toUserId()).isEqualTo(5L);
            assertThat(responses.get(0).status()).isEqualTo("PENDING");
            assertThat(responses.get(0).createdAt()).isEqualTo("2026-05-21T09:00:00");
            assertThat(responses.get(0).nickname()).isEqualTo("요청보낸유저");
            verify(friendRequestRepository).findAllByToUserAndStatusAndDeletedAtIsNull(me, FriendStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("친구 요청 전송")
    class 친구_요청_전송_테스트 {

        @Test
        @DisplayName("INF_UNITY_012: 상대방에게 PENDING 친구 요청을 생성한다")
        void sendFriendRequest_createsPendingFriendRequest() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User receiver = createUser(8L, "receiver@test.com", "상대방닉네임");
            FriendRequestCreateRequest request = new FriendRequestCreateRequest(receiver.getId());

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
            when(friendRequestRepository.existsActiveRelationBetween(me, receiver)).thenReturn(false);
            when(friendRequestRepository.save(org.mockito.ArgumentMatchers.any(FriendRequest.class)))
                    .thenAnswer(invocation -> {
                        FriendRequest saved = invocation.getArgument(0);
                        ReflectionTestUtils.setField(saved, "id", 3L);
                        ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2026, 5, 21, 12, 0));
                        return saved;
                    });

            FriendResponse response = friendRequestService.sendFriendRequest(me.getEmail(), request);

            //then
            assertThat(response.friendRequestId()).isEqualTo(3L);
            assertThat(response.fromUserId()).isEqualTo(5L);
            assertThat(response.toUserId()).isEqualTo(8L);
            assertThat(response.status()).isEqualTo("PENDING");
            assertThat(response.createdAt()).isEqualTo("2026-05-21T12:00:00");
            assertThat(response.nickname()).isEqualTo("상대방닉네임");
        }

        @Test
        @DisplayName("자기 자신에게 친구 요청을 보낼 수 없다")
        void sendFriendRequest_throwsBadRequestWhenRequestToSelf() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(userRepository.findById(me.getId())).thenReturn(Optional.of(me));

            //then
            assertBusinessException(
                    () -> friendRequestService.sendFriendRequest(me.getEmail(), new FriendRequestCreateRequest(me.getId())),
                    ErrorCode.FRIEND_REQUEST_SELF_NOT_ALLOWED
            );
            verify(friendRequestRepository, never()).save(org.mockito.ArgumentMatchers.any(FriendRequest.class));
        }

        @Test
        @DisplayName("이미 친구 관계이거나 보낸 요청이 있으면 Conflict 예외가 발생한다")
        void sendFriendRequest_throwsConflictWhenRelationAlreadyExists() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User receiver = createUser(8L, "receiver@test.com", "상대방닉네임");

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
            when(friendRequestRepository.existsActiveRelationBetween(me, receiver)).thenReturn(true);

            //then
            assertBusinessException(
                    () -> friendRequestService.sendFriendRequest(me.getEmail(), new FriendRequestCreateRequest(receiver.getId())),
                    ErrorCode.FRIEND_RELATION_ALREADY_EXISTS
            );
            verify(friendRequestRepository, never()).save(org.mockito.ArgumentMatchers.any(FriendRequest.class));
        }

        @Test
        @DisplayName("거절된 친구 요청은 새 친구 요청 전송을 막지 않는다")
        void sendFriendRequest_createsRequestWhenDeclinedRelationExists() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User receiver = createUser(8L, "receiver@test.com", "상대방닉네임");
            FriendRequestCreateRequest request = new FriendRequestCreateRequest(receiver.getId());

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
            when(friendRequestRepository.existsActiveRelationBetween(me, receiver)).thenReturn(false);
            when(friendRequestRepository.save(org.mockito.ArgumentMatchers.any(FriendRequest.class)))
                    .thenAnswer(invocation -> {
                        FriendRequest saved = invocation.getArgument(0);
                        ReflectionTestUtils.setField(saved, "id", 4L);
                        ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2026, 5, 21, 13, 0));
                        return saved;
                    });

            FriendResponse response = friendRequestService.sendFriendRequest(me.getEmail(), request);

            //then
            assertThat(response.friendRequestId()).isEqualTo(4L);
            assertThat(response.status()).isEqualTo("PENDING");
            assertThat(response.nickname()).isEqualTo("상대방닉네임");
        }
    }

    @Nested
    @DisplayName("친구 요청 수락")
    class 친구_요청_수락_테스트 {

        @Test
        @DisplayName("INF_UNITY_013: 받은 PENDING 요청을 ACCEPTED로 변경한다")
        void acceptFriendRequest_changesStatusToAccepted() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User sender = createUser(3L, "sender@test.com", "요청보낸유저");
            FriendRequest friendRequest = createFriendRequest(2L, sender, me, FriendStatus.PENDING,
                    LocalDateTime.of(2026, 5, 21, 9, 0));

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findById(friendRequest.getId())).thenReturn(Optional.of(friendRequest));

            FriendResponse response = friendRequestService.acceptFriendRequest(me.getEmail(), friendRequest.getId());

            //then
            assertThat(friendRequest.getStatus()).isEqualTo(FriendStatus.ACCEPTED);
            assertThat(response.status()).isEqualTo("ACCEPTED");
            assertThat(response.nickname()).isEqualTo("요청보낸유저");
        }

        @Test
        @DisplayName("본인에게 온 요청만 수락할 수 있다")
        void acceptFriendRequest_throwsBadRequestWhenNotReceiver() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User receiver = createUser(8L, "receiver@test.com", "상대방닉네임");
            FriendRequest friendRequest = createFriendRequest(2L, me, receiver, FriendStatus.PENDING,
                    LocalDateTime.of(2026, 5, 21, 9, 0));

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findById(friendRequest.getId())).thenReturn(Optional.of(friendRequest));

            //then
            assertBusinessException(
                    () -> friendRequestService.acceptFriendRequest(me.getEmail(), friendRequest.getId()),
                    ErrorCode.FRIEND_REQUEST_RECEIVER_ONLY
            );
        }

        @Test
        @DisplayName("친구 요청을 찾을 수 없으면 Not Found 예외가 발생한다")
        void acceptFriendRequest_throwsNotFoundWhenRequestDoesNotExist() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findById(99L)).thenReturn(Optional.empty());

            //then
            assertBusinessException(
                    () -> friendRequestService.acceptFriendRequest(me.getEmail(), 99L),
                    ErrorCode.FRIEND_REQUEST_NOT_FOUND
            );
        }
    }

    @Nested
    @DisplayName("친구 요청 거절")
    class 친구_요청_거절_테스트 {

        @Test
        @DisplayName("INF_UNITY_014: 받은 친구 요청을 DECLINED로 변경한다")
        void declineFriendRequest_changesStatusToDeclined() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User sender = createUser(3L, "sender@test.com", "요청보낸유저");
            FriendRequest friendRequest = createFriendRequest(2L, sender, me, FriendStatus.PENDING,
                    LocalDateTime.of(2026, 5, 21, 9, 0));

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findById(friendRequest.getId())).thenReturn(Optional.of(friendRequest));

            friendRequestService.declineFriendRequest(me.getEmail(), friendRequest.getId());

            //then
            assertThat(friendRequest.getStatus()).isEqualTo(FriendStatus.DECLINED);
        }

        @Test
        @DisplayName("본인에게 온 요청만 거절할 수 있다")
        void declineFriendRequest_throwsBadRequestWhenNotReceiver() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User receiver = createUser(8L, "receiver@test.com", "상대방닉네임");
            FriendRequest friendRequest = createFriendRequest(2L, me, receiver, FriendStatus.PENDING,
                    LocalDateTime.of(2026, 5, 21, 9, 0));

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findById(friendRequest.getId())).thenReturn(Optional.of(friendRequest));

            //then
            assertBusinessException(
                    () -> friendRequestService.declineFriendRequest(me.getEmail(), friendRequest.getId()),
                    ErrorCode.FRIEND_REQUEST_DECLINE_RECEIVER_ONLY
            );
        }
    }

    @Nested
    @DisplayName("친구 요청 취소")
    class 친구_요청_취소_테스트 {

        @Test
        @DisplayName("INF_UNITY_015: 내가 보낸 친구 요청을 CANCELED로 변경하고 삭제 시간을 기록한다")
        void cancelFriendRequest_changesStatusToCanceledAndDeletes() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User receiver = createUser(8L, "receiver@test.com", "상대방닉네임");
            FriendRequest friendRequest = createFriendRequest(3L, me, receiver, FriendStatus.PENDING,
                    LocalDateTime.of(2026, 5, 21, 12, 0));

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findById(friendRequest.getId())).thenReturn(Optional.of(friendRequest));

            friendRequestService.cancelFriendRequest(me.getEmail(), friendRequest.getId());

            //then
            assertThat(friendRequest.getStatus()).isEqualTo(FriendStatus.CANCELED);
            assertThat(friendRequest.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("본인이 보낸 요청만 취소할 수 있다")
        void cancelFriendRequest_throwsBadRequestWhenNotSender() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User sender = createUser(3L, "sender@test.com", "요청보낸유저");
            FriendRequest friendRequest = createFriendRequest(3L, sender, me, FriendStatus.PENDING,
                    LocalDateTime.of(2026, 5, 21, 12, 0));

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findById(friendRequest.getId())).thenReturn(Optional.of(friendRequest));

            //then
            assertBusinessException(
                    () -> friendRequestService.cancelFriendRequest(me.getEmail(), friendRequest.getId()),
                    ErrorCode.FRIEND_REQUEST_SENDER_ONLY
            );
        }
    }

    @Nested
    @DisplayName("친구 관계 삭제")
    class 친구_관계_삭제_테스트 {

        @Test
        @DisplayName("INF_UNITY_016: ACCEPTED 친구 관계를 삭제한다")
        void deleteFriend_deletesAcceptedFriendRelation() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User friend = createUser(8L, "friend@test.com", "친구닉네임");
            FriendRequest friendRequest = createFriendRequest(1L, me, friend, FriendStatus.ACCEPTED,
                    LocalDateTime.of(2025, 1, 10, 12, 0));

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findAcceptedRelationsBetween(me, friend.getId(), FriendStatus.ACCEPTED))
                    .thenReturn(List.of(friendRequest));

            friendRequestService.deleteFriend(me.getEmail(), friend.getId());

            //then
            assertThat(friendRequest.getDeletedAt()).isNotNull();
            verify(friendRequestRepository).findAcceptedRelationsBetween(me, friend.getId(), FriendStatus.ACCEPTED);
        }

        @Test
        @DisplayName("INF_UNITY_016: 요청을 받은 유저도 ACCEPTED 친구 관계를 삭제할 수 있다")
        void deleteFriend_deletesAcceptedRelationWhenAuthenticatedUserIsToUser() {
            //given
            User sender = createUser(5L, "sender@test.com", "요청보낸유저");
            User me = createUser(8L, "gamer@test.com", "게이머");
            FriendRequest friendRequest = createFriendRequest(1L, sender, me, FriendStatus.ACCEPTED,
                    LocalDateTime.of(2025, 1, 10, 12, 0));

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findAcceptedRelationsBetween(me, sender.getId(), FriendStatus.ACCEPTED))
                    .thenReturn(List.of(friendRequest));

            friendRequestService.deleteFriend(me.getEmail(), sender.getId());

            //then
            assertThat(friendRequest.getDeletedAt()).isNotNull();
            verify(friendRequestRepository).findAcceptedRelationsBetween(me, sender.getId(), FriendStatus.ACCEPTED);
        }

        @Test
        @DisplayName("INF_UNITY_016: 중복된 양방향 ACCEPTED 친구 관계를 모두 삭제한다")
        void deleteFriend_deletesDuplicatedAcceptedRelationsInBothDirections() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            User friend = createUser(8L, "friend@test.com", "친구닉네임");
            FriendRequest sentRelation = createFriendRequest(1L, me, friend, FriendStatus.ACCEPTED,
                    LocalDateTime.of(2025, 1, 10, 12, 0));
            FriendRequest receivedRelation = createFriendRequest(2L, friend, me, FriendStatus.ACCEPTED,
                    LocalDateTime.of(2025, 1, 10, 12, 1));

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findAcceptedRelationsBetween(me, friend.getId(), FriendStatus.ACCEPTED))
                    .thenReturn(List.of(sentRelation, receivedRelation));

            friendRequestService.deleteFriend(me.getEmail(), friend.getId());

            //then
            assertThat(sentRelation.getDeletedAt()).isNotNull();
            assertThat(receivedRelation.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("친구 관계가 아니면 Not Found 예외가 발생한다")
        void deleteFriend_throwsNotFoundWhenRelationDoesNotExist() {
            //given
            User me = createUser(5L, "gamer@test.com", "게이머");
            Long friendUserId = 8L;

            //when
            when(userRepository.findByEmail(me.getEmail())).thenReturn(Optional.of(me));
            when(friendRequestRepository.findAcceptedRelationsBetween(me, friendUserId, FriendStatus.ACCEPTED))
                    .thenReturn(List.of());

            //then
            assertBusinessException(
                    () -> friendRequestService.deleteFriend(me.getEmail(), friendUserId),
                    ErrorCode.FRIEND_RELATION_NOT_FOUND
            );
        }
    }

    private User createUser(Long id, String email, String nickname) {
        User user = User.builder()
                .email(email)
                .password("encoded-password")
                .nickname(nickname)
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private FriendRequest createFriendRequest(
            Long id,
            User fromUser,
            User toUser,
            FriendStatus status,
            LocalDateTime createdAt
    ) {
        FriendRequest friendRequest = FriendRequest.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .status(status)
                .build();
        ReflectionTestUtils.setField(friendRequest, "id", id);
        ReflectionTestUtils.setField(friendRequest, "createdAt", createdAt);
        return friendRequest;
    }

    private static void assertBusinessException(Runnable action, ErrorCode expectedErrorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode()).isEqualTo(expectedErrorCode);
                    assertThat(businessException.getMessage()).isEqualTo(expectedErrorCode.getDescription());
                });
    }
}
