package com.yunhao.superai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "app_type")
    @Builder.Default
    private AppType appType = AppType.FITNESS;

    @Column(name = "message_count")
    @Builder.Default
    private Integer messageCount = 0;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean isArchived = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 应用类型枚举
    public enum AppType {
        FITNESS("运动助手"),
        MANUS("通用助手");

        private final String displayName;

        AppType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}