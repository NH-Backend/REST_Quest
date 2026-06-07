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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
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

        return FriendResponse.from(friendRequest, toUser);
    }

    @Transactional
    public FriendResponse acceptFriendRequest(String email, Long requestId) {
        User user = findAuthenticatedUser(email);
        FriendRequest friendRequest = findPendingFriendRequest(requestId);

        if (!Objects.equals(friendRequest.getToUser().getId(), user.getId())) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_RECEIVER_ONLY);
        }

        friendRequest.accept();

        return FriendResponse.from(friendRequest, friendRequest.getFromUser());
    }

    @Transactional
    public void declineFriendRequest(String email, Long requestId) {
        User user = findAuthenticatedUser(email);
        FriendRequest friendRequest = findPendingFriendRequest(requestId);

        if (!Objects.equals(friendRequest.getToUser().getId(), user.getId())) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_DECLINE_RECEIVER_ONLY);
        }

        friendRequest.decline();
    }

    @Transactional
    public void cancelFriendRequest(String email, Long requestId) {
        User user = findAuthenticatedUser(email);
        FriendRequest friendRequest = findPendingFriendRequest(requestId);

        if (!Objects.equals(friendRequest.getFromUser().getId(), user.getId())) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_SENDER_ONLY);
        }

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

    private FriendRequest findPendingFriendRequest(Long requestId) {
        return friendRequestRepository.findByIdAndStatusAndDeletedAtIsNull(requestId, FriendStatus.PENDING)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));
    }

    private User getCounterpart(FriendRequest friendRequest, User user) {
        if (Objects.equals(friendRequest.getFromUser().getId(), user.getId())) {
            return friendRequest.getToUser();
        }

        return friendRequest.getFromUser();
    }
}
