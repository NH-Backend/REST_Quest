package io.nh_backend.rest_quest.user.repository;

import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser(User user);
}
