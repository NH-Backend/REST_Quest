package io.nh_backend.rest_quest.friend_request.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.friend_request.domain.FriendRequest;
import io.nh_backend.rest_quest.friend_request.domain.FriendStatus;
import io.nh_backend.rest_quest.friend_request.dto.FriendRequestCreateRequest;
import io.nh_backend.rest_quest.friend_request.dto.FriendResponse;
import io.nh_backend.rest_quest.friend_request.repository.FriendRequestRepository;
import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<FriendResponse> getAcceptedFriends(String email) {
        User user = findAuthenticatedUser(email);

        return friendRequestRepository.findAcceptedFriends(user, FriendStatus.ACCEPTED)
                .stream()
                .map(friendRequest -> FriendResponse.from(friendRequest, getCounterpart(friendRequest, user)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendResponse> getPendingRequestsToMe(String email) {
        User user = findAuthenticatedUser(email);

        return friendRequestRepository.findAllByToUserAndStatusAndDeletedAtIsNull(user, FriendStatus.PENDING)
                .stream()
                .map(friendRequest -> FriendResponse.from(friendRequest, friendRequest.getFromUser()))
                .toList();
    }

    @Transactional
    public FriendResponse sendFriendRequest(String email, FriendRequestCreateRequest request) {
        User fromUser = findAuthenticatedUser(email);
        User toUser = userRepository.findById(request.toUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED_USER));

        if (Objects.equals(fromUser.getId(), toUser.getId())) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_SELF_NOT_ALLOWED);
        }

        if (friendRequestRepository.existsActiveRelationBetween(fromUser, toUser)) {
            throw new BusinessException(ErrorCode.FRIEND_RELATION_ALREADY_EXISTS);
        }

        FriendRequest friendRequest = friendRequestRepository.save(FriendRequest.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .status(FriendStatus.PENDING)
                .build());
        log.info("친구요청 메시지 보내기");

        return FriendResponse.from(friendRequest, toUser);
    }

    @Transactional
    public FriendResponse acceptFriendRequest(String email, Long requestId) {
        User user = findAuthenticatedUser(email);
        FriendRequest friendRequest = findPendingFriendRequestToUser(requestId, user);

        friendRequest.accept();

        return FriendResponse.from(friendRequest, friendRequest.getFromUser());
    }

    @Transactional
    public void declineFriendRequest(String email, Long requestId) {
        User user = findAuthenticatedUser(email);
        FriendRequest friendRequest = findPendingFriendRequestToUser(requestId, user);
        log.info("받은친구요청 거절");
        friendRequest.decline();
    }

    @Transactional
    public void cancelFriendRequest(String email, Long requestId) {
        User user = findAuthenticatedUser(email);
        FriendRequest friendRequest = findPendingFriendRequestFromUser(requestId, user);
        log.info("보낸친구요청 취소");
        friendRequest.cancel();
    }

    @Transactional
    public void deleteFriend(String email, Long friendUserId) {
        User user = findAuthenticatedUser(email);
        List<FriendRequest> friendRequests = friendRequestRepository
                .findAcceptedRelationsBetween(user, friendUserId, FriendStatus.ACCEPTED);

        if (friendRequests.isEmpty()) {
            throw new BusinessException(ErrorCode.FRIEND_RELATION_NOT_FOUND);
        }

        friendRequests.forEach(FriendRequest::delete);
    }

    private User findAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED_USER));
    }

    private FriendRequest findPendingFriendRequestToUser(Long requestId, User user) {
        return friendRequestRepository
                .findByIdAndToUserAndStatusAndDeletedAtIsNull(requestId, user, FriendStatus.PENDING)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));
    }

    private FriendRequest findPendingFriendRequestFromUser(Long requestId, User user) {
        return friendRequestRepository
                .findByIdAndFromUserAndStatusAndDeletedAtIsNull(requestId, user, FriendStatus.PENDING)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));
    }

    private User getCounterpart(FriendRequest friendRequest, User user) {
        if (Objects.equals(friendRequest.getFromUser().getId(), user.getId())) {
            return friendRequest.getToUser();
        }

        return friendRequest.getFromUser();
    }
}
