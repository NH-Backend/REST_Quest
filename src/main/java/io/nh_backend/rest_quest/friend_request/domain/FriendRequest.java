package io.nh_backend.rest_quest.friend_request.domain;

import io.nh_backend.rest_quest.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Null;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "friend_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    @Builder
    public FriendRequest(User fromUser, User toUser, FriendStatus status) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.deletedAt = null;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
