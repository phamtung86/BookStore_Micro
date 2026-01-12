package com.bookstore.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user", columnList = "user_id"),
        @Index(name = "idx_notifications_status", columnList = "status"),
        @Index(name = "idx_notifications_channel", columnList = "channel"),
        @Index(name = "idx_notifications_reference", columnList = "reference_type, reference_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    @ToString.Exclude
    private NotificationTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationTemplate.NotificationChannel channel;

    @Column(length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "recipient_email", length = 100)
    private String recipientEmail;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // ORDER, PAYMENT, PROMOTION

    @Column(name = "reference_id", length = 50)
    private String referenceId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationStatus {
        PENDING, // Đang chờ gửi
        SENT, // Đã gửi
        DELIVERED, // Đã gửi thành công
        FAILED, // Gửi thất bại
        READ // Đã đọc
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsDelivered() {
        this.status = NotificationStatus.DELIVERED;
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }

    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }

    public boolean isEmail() {
        return channel == NotificationTemplate.NotificationChannel.EMAIL;
    }

    public boolean isSms() {
        return channel == NotificationTemplate.NotificationChannel.SMS;
    }

    public boolean isPush() {
        return channel == NotificationTemplate.NotificationChannel.PUSH;
    }

    public boolean isInApp() {
        return channel == NotificationTemplate.NotificationChannel.IN_APP;
    }
}
