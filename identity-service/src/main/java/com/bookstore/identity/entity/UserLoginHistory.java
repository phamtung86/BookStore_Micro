package com.bookstore.identity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_login_history", indexes = {
        @Index(name = "idx_login_history_user", columnList = "user_id"),
        @Index(name = "idx_login_history_date", columnList = "login_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "login_at", nullable = false)
    @Builder.Default
    private LocalDateTime loginAt = LocalDateTime.now();

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    @Builder.Default
    private DeviceType deviceType = DeviceType.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_status", nullable = false)
    private LoginStatus loginStatus;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    public enum DeviceType {
        WEB, MOBILE, TABLET, UNKNOWN
    }

    public enum LoginStatus {
        SUCCESS, FAILED, BLOCKED
    }
}
