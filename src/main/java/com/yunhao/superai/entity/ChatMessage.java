package com.yunhao.superai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 50)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    // 使用TEXT字段存储JSON数据
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachmentsJson;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 消息类型枚举
    public enum MessageType {
        USER("用户消息"),
        ASSISTANT("助手回复"),
        SYSTEM("系统消息");

        private final String displayName;

        MessageType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}