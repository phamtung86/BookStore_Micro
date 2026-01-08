package com.bookstore.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_templates", indexes = {
        @Index(name = "idx_templates_code", columnList = "code"),
        @Index(name = "idx_templates_channel", columnList = "channel")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // e.g., ORDER_CREATED, ORDER_SHIPPED, WELCOME

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum NotificationChannel {
        EMAIL,
        SMS,
        PUSH,
        IN_APP
    }

    public String processBody(java.util.Map<String, String> variables) {
        String result = this.body;
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("#{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    public String processSubject(java.util.Map<String, String> variables) {
        if (this.subject == null)
            return null;
        String result = this.subject;
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("#{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
