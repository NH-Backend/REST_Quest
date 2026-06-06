package io.nh_backend.rest_quest.friend_request.controller;

import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.common.dto.ApiResponse;
import io.nh_backend.rest_quest.friend_request.dto.FriendRequestCreateRequest;
import io.nh_backend.rest_quest.friend_request.dto.FriendResponse;
import io.nh_backend.rest_quest.friend_request.service.FriendRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @GetMapping("/api/v1/users/me/friends")
    public ApiResponse<List<FriendResponse>> getAcceptedFriends(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return ApiResponse.ok(
                friendRequestService.getAcceptedFriends(principal.getName()),
                null
        );
    }

    @GetMapping("/api/v1/users/me/friends/requests")
    public ApiResponse<List<FriendResponse>> getPendingRequestsToMe(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return ApiResponse.ok(
                friendRequestService.getPendingRequestsToMe(principal.getName()),
                null
        );
    }

    @PostMapping("/api/v1/users/me/friends/requests")
    public ApiResponse<FriendResponse> sendFriendRequest(
            Principal principal,
            @Valid @RequestBody FriendRequestCreateRequest request
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return ApiResponse.ok(
                friendRequestService.sendFriendRequest(principal.getName(), request),
                SuccessCode.FRIEND_REQUEST_SENT.getSuccessMessage()
        );
    }

    @PostMapping("/api/v1/users/me/friends/requests/{requestId}/accept")
    public ApiResponse<FriendResponse> acceptFriendRequest(
            Principal principal,
            @PathVariable("requestId") Long requestId
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return ApiResponse.ok(
                friendRequestService.acceptFriendRequest(principal.getName(), requestId),
                SuccessCode.FRIEND_REQUEST_ACCEPTED.getSuccessMessage()
        );
    }

    @PostMapping("/api/v1/users/me/friends/requests/{requestId}/decline")
    public ApiResponse<Void> declineFriendRequest(
            Principal principal,
            @PathVariable("requestId") Long requestId
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        friendRequestService.declineFriendRequest(principal.getName(), requestId);

        return ApiResponse.ok(
                null,
                SuccessCode.FRIEND_REQUEST_DECLINED.getSuccessMessage()
        );
    }

    @DeleteMapping("/api/v1/users/me/friends/requests/{requestId}")
    public ApiResponse<Void> cancelFriendRequest(
            Principal principal,
            @PathVariable("requestId") Long requestId
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        friendRequestService.cancelFriendRequest(principal.getName(), requestId);

        return ApiResponse.ok(
                null,
                SuccessCode.FRIEND_REQUEST_CANCELED.getSuccessMessage()
        );
    }
}
