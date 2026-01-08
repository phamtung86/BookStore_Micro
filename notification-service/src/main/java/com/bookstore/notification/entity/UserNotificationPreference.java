package com.bookstore.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notification_preferences", indexes = {
        @Index(name = "idx_notification_prefs_user", columnList = "user_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "email_order_updates")
    @Builder.Default
    private Boolean emailOrderUpdates = true;

    @Column(name = "email_promotions")
    @Builder.Default
    private Boolean emailPromotions = true;

    @Column(name = "email_newsletter")
    @Builder.Default
    private Boolean emailNewsletter = false;

    @Column(name = "sms_order_updates")
    @Builder.Default
    private Boolean smsOrderUpdates = true;

    @Column(name = "sms_promotions")
    @Builder.Default
    private Boolean smsPromotions = false;

    @Column(name = "push_enabled")
    @Builder.Default
    private Boolean pushEnabled = true;

    @Column(name = "push_order_updates")
    @Builder.Default
    private Boolean pushOrderUpdates = true;

    @Column(name = "push_promotions")
    @Builder.Default
    private Boolean pushPromotions = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean shouldSendEmail(String notificationType) {
        return switch (notificationType) {
            case "ORDER_UPDATE" -> emailOrderUpdates;
            case "PROMOTION" -> emailPromotions;
            case "NEWSLETTER" -> emailNewsletter;
            default -> true;
        };
    }

    public boolean shouldSendSms(String notificationType) {
        return switch (notificationType) {
            case "ORDER_UPDATE" -> smsOrderUpdates;
            case "PROMOTION" -> smsPromotions;
            default -> false;
        };
    }

    public boolean shouldSendPush(String notificationType) {
        if (!pushEnabled)
            return false;
        return switch (notificationType) {
            case "ORDER_UPDATE" -> pushOrderUpdates;
            case "PROMOTION" -> pushPromotions;
            default -> true;
        };
    }

    public static UserNotificationPreference createDefault(Long userId) {
        return UserNotificationPreference.builder()
                .userId(userId)
                .emailOrderUpdates(true)
                .emailPromotions(true)
                .emailNewsletter(false)
                .smsOrderUpdates(true)
                .smsPromotions(false)
                .pushEnabled(true)
                .pushOrderUpdates(true)
                .pushPromotions(true)
                .build();
    }
}
