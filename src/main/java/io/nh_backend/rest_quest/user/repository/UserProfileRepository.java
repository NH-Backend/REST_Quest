package io.nh_backend.rest_quest.user.repository;

import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser(User user);
}
