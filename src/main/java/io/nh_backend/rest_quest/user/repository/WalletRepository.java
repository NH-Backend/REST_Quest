package io.nh_backend.rest_quest.user.repository;

import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
