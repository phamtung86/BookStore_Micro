package com.bookstore.identity.repository;

import com.bookstore.identity.entity.UserLoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Long> {

    Page<UserLoginHistory> findByUserIdOrderByLoginAtDesc(Long userId, Pageable pageable);

    Optional<UserLoginHistory> findTopByUserIdOrderByLoginAtDesc(Long userId);

    @Query("SELECT h FROM UserLoginHistory h WHERE h.user.id = :userId " +
            "AND h.loginStatus = 'SUCCESS' " +
            "AND h.loginAt BETWEEN :startDate AND :endDate " +
            "ORDER BY h.loginAt DESC")
    List<UserLoginHistory> findSuccessfulLoginsBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(h) FROM UserLoginHistory h WHERE h.user.id = :userId " +
            "AND h.loginStatus = 'FAILED' " +
            "AND h.loginAt > :since")
    long countFailedLoginsSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    List<UserLoginHistory> findByIpAddressOrderByLoginAtDesc(String ipAddress);

    @Query("SELECT COUNT(h) > 0 FROM UserLoginHistory h WHERE h.user.id = :userId " +
            "AND h.userAgent = :userAgent AND h.loginStatus = 'SUCCESS'")
    boolean isKnownDevice(@Param("userId") Long userId, @Param("userAgent") String userAgent);
}
