package com.swiftpay.repository;

import com.swiftpay.domain.UserAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from UserAccount account where account.userId = :userId")
    Optional<UserAccount> findByIdForUpdate(@Param("userId") UUID userId);
}
