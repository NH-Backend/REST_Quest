package io.nh_backend.rest_quest.friend_request.controller;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.common.eventhandler.GlobalExceptionHandler;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.friend_request.dto.FriendRequestCreateRequest;
import io.nh_backend.rest_quest.friend_request.dto.FriendResponse;
import io.nh_backend.rest_quest.friend_request.service.FriendRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FriendRequestControllerTest {

    private FriendRequestService friendRequestService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        friendRequestService = mock(FriendRequestService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FriendRequestController(friendRequestService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("친구 목록 조회")
    class 친구_목록_조회_테스트 {

        @Test
        @DisplayName("INF_UNITY_010: 수락된 친구 목록을 반환한다")
        void getAcceptedFriends_returnsAcceptedFriendList() throws Exception {
            //given
            String email = "gamer@test.com";
            FriendResponse response = new FriendResponse(
                    1L,
                    5L,
                    8L,
                    "ACCEPTED",
                    "2025-01-10T12:00:00",
                    "친구닉네임"
            );

            //when
            when(friendRequestService.getAcceptedFriends(email)).thenReturn(List.of(response));

            mockMvc.perform(get("/api/v1/users/me/friends")
                            .principal(() -> email))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").doesNotExist())
                    .andExpect(jsonPath("$.data[0].friendRequestId").value(1L))
                    .andExpect(jsonPath("$.data[0].fromUserId").value(5L))
                    .andExpect(jsonPath("$.data[0].toUserId").value(8L))
                    .andExpect(jsonPath("$.data[0].status").value("ACCEPTED"))
                    .andExpect(jsonPath("$.data[0].createdAt").value("2025-01-10T12:00:00"))
                    .andExpect(jsonPath("$.data[0].nickname").value("친구닉네임"))
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(friendRequestService).getAcceptedFriends(email);
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 친구 목록을 조회할 수 없다")
        void getAcceptedFriends_returnsUnauthorizedWhenPrincipalDoesNotExist() throws Exception {
            mockMvc.perform(get("/api/v1/users/me/friends"))
            //then
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("받은 친구 요청 목록 조회")
    class 받은_친구_요청_목록_조회_테스트 {

        @Test
        @DisplayName("INF_UNITY_011: 내게 온 PENDING 친구 요청 목록을 반환한다")
        void getPendingRequestsToMe_returnsPendingRequestList() throws Exception {
            //given
            String email = "gamer@test.com";
            FriendResponse response = new FriendResponse(
                    2L,
                    3L,
                    5L,
                    "PENDING",
                    "2026-05-21T09:00:00",
                    "요청보낸유저"
            );

            //when
            when(friendRequestService.getPendingRequestsToMe(email)).thenReturn(List.of(response));

            mockMvc.perform(get("/api/v1/users/me/friends/requests")
                            .principal(() -> email))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").doesNotExist())
                    .andExpect(jsonPath("$.data[0].friendRequestId").value(2L))
                    .andExpect(jsonPath("$.data[0].fromUserId").value(3L))
                    .andExpect(jsonPath("$.data[0].toUserId").value(5L))
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                    .andExpect(jsonPath("$.data[0].createdAt").value("2026-05-21T09:00:00"))
                    .andExpect(jsonPath("$.data[0].nickname").value("요청보낸유저"))
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(friendRequestService).getPendingRequestsToMe(email);
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 받은 친구 요청 목록을 조회할 수 없다")
        void getPendingRequestsToMe_returnsUnauthorizedWhenPrincipalDoesNotExist() throws Exception {
            mockMvc.perform(get("/api/v1/users/me/friends/requests"))
            //then
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("친구 요청 전송")
    class 친구_요청_전송_테스트 {

        @Test
        @DisplayName("INF_UNITY_012: 상대방에게 친구 요청을 보낸다")
        void sendFriendRequest_returnsPendingFriendRequest() throws Exception {
            //given
            String email = "gamer@test.com";
            FriendRequestCreateRequest request = new FriendRequestCreateRequest(8L);
            FriendResponse response = new FriendResponse(
                    3L,
                    5L,
                    8L,
                    "PENDING",
                    "2026-05-21T12:00:00",
                    "상대방닉네임"
            );

            //when
            when(friendRequestService.sendFriendRequest(email, request)).thenReturn(response);

            mockMvc.perform(post("/api/v1/users/me/friends/requests")
                            .principal(() -> email)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "toUserId": 8
                                    }
                                    """))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(SuccessCode.FRIEND_REQUEST_SENT.getSuccessMessage()))
                    .andExpect(jsonPath("$.data.friendRequestId").value(3L))
                    .andExpect(jsonPath("$.data.fromUserId").value(5L))
                    .andExpect(jsonPath("$.data.toUserId").value(8L))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.createdAt").value("2026-05-21T12:00:00"))
                    .andExpect(jsonPath("$.data.nickname").value("상대방닉네임"))
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(friendRequestService).sendFriendRequest(email, request);
        }

        @Test
        @DisplayName("자기 자신에게 친구 요청을 보낼 수 없다")
        void sendFriendRequest_returnsBadRequestWhenRequestToSelf() throws Exception {
            //given
            String email = "gamer@test.com";
            FriendRequestCreateRequest request = new FriendRequestCreateRequest(5L);

            //when
            when(friendRequestService.sendFriendRequest(email, request))
                    .thenThrow(new BusinessException(ErrorCode.FRIEND_REQUEST_SELF_NOT_ALLOWED));

            mockMvc.perform(post("/api/v1/users/me/friends/requests")
                            .principal(() -> email)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "toUserId": 5
                                    }
                                    """))
            //then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(ErrorCode.FRIEND_REQUEST_SELF_NOT_ALLOWED.getDescription()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("이미 친구 관계이거나 보낸 요청이 있으면 Conflict 응답을 반환한다")
        void sendFriendRequest_returnsConflictWhenRelationAlreadyExists() throws Exception {
            //given
            String email = "gamer@test.com";
            FriendRequestCreateRequest request = new FriendRequestCreateRequest(8L);

            //when
            when(friendRequestService.sendFriendRequest(email, request))
                    .thenThrow(new BusinessException(ErrorCode.FRIEND_RELATION_ALREADY_EXISTS));

            mockMvc.perform(post("/api/v1/users/me/friends/requests")
                            .principal(() -> email)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "toUserId": 8
                                    }
                                    """))
            //then
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(ErrorCode.FRIEND_RELATION_ALREADY_EXISTS.getDescription()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("친구 요청 수락")
    class 친구_요청_수락_테스트 {

        @Test
        @DisplayName("INF_UNITY_013: 받은 친구 요청을 수락한다")
        void acceptFriendRequest_returnsAcceptedFriendRequest() throws Exception {
            //given
            String email = "gamer@test.com";
            FriendResponse response = new FriendResponse(
                    2L,
                    3L,
                    5L,
                    "ACCEPTED",
                    "2026-05-21T09:00:00",
                    "요청보낸유저"
            );

            //when
            when(friendRequestService.acceptFriendRequest(email, 2L)).thenReturn(response);

            mockMvc.perform(post("/api/v1/users/me/friends/requests/2/accept")
                            .principal(() -> email))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(SuccessCode.FRIEND_REQUEST_ACCEPTED.getSuccessMessage()))
                    .andExpect(jsonPath("$.data.friendRequestId").value(2L))
                    .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                    .andExpect(jsonPath("$.data.nickname").value("요청보낸유저"))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("본인에게 온 요청만 수락할 수 있다")
        void acceptFriendRequest_returnsBadRequestWhenNotReceiver() throws Exception {
            //given
            String email = "gamer@test.com";

            //when
            when(friendRequestService.acceptFriendRequest(email, 2L))
                    .thenThrow(new BusinessException(ErrorCode.FRIEND_REQUEST_RECEIVER_ONLY));

            mockMvc.perform(post("/api/v1/users/me/friends/requests/2/accept")
                            .principal(() -> email))
            //then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(ErrorCode.FRIEND_REQUEST_RECEIVER_ONLY.getDescription()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("친구 요청 거절")
    class 친구_요청_거절_테스트 {

        @Test
        @DisplayName("INF_UNITY_014: 받은 친구 요청을 거절한다")
        void declineFriendRequest_returnsSuccess() throws Exception {
            //given
            String email = "gamer@test.com";

            mockMvc.perform(post("/api/v1/users/me/friends/requests/2/decline")
                            .principal(() -> email))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(SuccessCode.FRIEND_REQUEST_DECLINED.getSuccessMessage()))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(friendRequestService).declineFriendRequest(email, 2L);
        }

        @Test
        @DisplayName("본인에게 온 요청만 거절할 수 있다")
        void declineFriendRequest_returnsBadRequestWhenNotReceiver() throws Exception {
            //given
            String email = "gamer@test.com";

            //when
            doThrow(new BusinessException(ErrorCode.FRIEND_REQUEST_DECLINE_RECEIVER_ONLY))
                    .when(friendRequestService).declineFriendRequest(email, 2L);

            mockMvc.perform(post("/api/v1/users/me/friends/requests/2/decline")
                            .principal(() -> email))
            //then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(ErrorCode.FRIEND_REQUEST_DECLINE_RECEIVER_ONLY.getDescription()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("친구 요청 취소")
    class 친구_요청_취소_테스트 {

        @Test
        @DisplayName("INF_UNITY_015: 내가 보낸 친구 요청을 취소한다")
        void cancelFriendRequest_returnsSuccess() throws Exception {
            //given
            String email = "gamer@test.com";

            mockMvc.perform(delete("/api/v1/users/me/friends/requests/3")
                            .principal(() -> email))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(SuccessCode.FRIEND_REQUEST_CANCELED.getSuccessMessage()))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(friendRequestService).cancelFriendRequest(email, 3L);
        }

        @Test
        @DisplayName("본인이 보낸 요청만 취소할 수 있다")
        void cancelFriendRequest_returnsBadRequestWhenNotSender() throws Exception {
            //given
            String email = "gamer@test.com";

            //when
            doThrow(new BusinessException(ErrorCode.FRIEND_REQUEST_SENDER_ONLY))
                    .when(friendRequestService).cancelFriendRequest(email, 3L);

            mockMvc.perform(delete("/api/v1/users/me/friends/requests/3")
                            .principal(() -> email))
            //then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(ErrorCode.FRIEND_REQUEST_SENDER_ONLY.getDescription()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("친구 관계 삭제")
    class 친구_관계_삭제_테스트 {

        @Test
        @DisplayName("INF_UNITY_016: 친구 관계를 삭제한다")
        void deleteFriend_returnsSuccess() throws Exception {
            //given
            String email = "gamer@test.com";

            mockMvc.perform(delete("/api/v1/users/me/friends/8")
                            .principal(() -> email))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(SuccessCode.FRIEND_DELETED.getSuccessMessage()))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(friendRequestService).deleteFriend(email, 8L);
        }

        @Test
        @DisplayName("친구 관계가 아니면 Not Found 응답을 반환한다")
        void deleteFriend_returnsNotFoundWhenRelationDoesNotExist() throws Exception {
            //given
            String email = "gamer@test.com";

            //when
            doThrow(new BusinessException(ErrorCode.FRIEND_RELATION_NOT_FOUND))
                    .when(friendRequestService).deleteFriend(email, 8L);

            mockMvc.perform(delete("/api/v1/users/me/friends/8")
                            .principal(() -> email))
            //then
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(ErrorCode.FRIEND_RELATION_NOT_FOUND.getDescription()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }
}
