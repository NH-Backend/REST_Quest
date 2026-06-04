package io.nh_backend.rest_quest.user.repository;

import io.nh_backend.rest_quest.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
